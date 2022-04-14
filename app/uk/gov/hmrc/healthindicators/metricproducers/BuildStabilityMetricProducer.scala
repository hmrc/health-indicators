/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.healthindicators.metricproducers

import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.{JenkinsBuildReport, JenkinsBuildStatus, JenkinsConnector, JenkinsUrl, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.metricproducers.BuildStabilityMetricProducer.getResultType
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Duration, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BuildStabilityMetricProducer @Inject() (
  jenkinsConnector: JenkinsConnector,
  teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector
)(implicit val ec: ExecutionContext)
    extends MetricProducer {

  private val logger = Logger(this.getClass)

  override def produce(repo: String): Future[Metric] = {

    implicit val hc = HeaderCarrier()
    logger.debug(s"Metric BuildStability for: $repo")

    for {
      maybeUrl    <- teamsAndRepositoriesConnector.getJenkinsUrl(repo)
      buildReport <- Future.traverse(maybeUrl.toSeq)(url => jenkinsConnector.getBuildJob(url.jenkinsURL)).map(_.flatten.headOption)
      result      = buildReport
                    .map(i => getResultType(i))
                    .getOrElse(Result(JenkinsBuildNotFound, s"No Jenkins Build Found for: $repo", None))
    } yield Metric(BuildStabilityMetricType, Seq(result))
  }
}

object BuildStabilityMetricProducer {
  def getResultType(jenkinsBuildReport: JenkinsBuildReport): Result =
    jenkinsBuildReport.lastCompletedBuild match {
      case Some(JenkinsBuildStatus("FAILURE", timeStamp)) if Duration.between(timeStamp, Instant.now()).toDays > 2 =>
        Result(JenkinsBuildUnstable, "Build Unstable: has been broken for more than 2 days", None)

      case Some(JenkinsBuildStatus("FAILURE", _)) =>
        Result(JenkinsBuildStable, "Build Stable: Build recently failed", None)

      case Some(JenkinsBuildStatus("SUCCESS", timeStamp)) if Duration.between(timeStamp, Instant.now()).toDays > 300 =>
        Result(JenkinsBuildOutdated, "Build Outdated: Not been built in the last 300 days", None)

      case Some(JenkinsBuildStatus("SUCCESS", _)) =>
        Result(JenkinsBuildStable, "Build Stable: Everything is good", None)

      case Some(JenkinsBuildStatus(status, _)) => Result(JenkinsBuildNotFound, s"Unknown Status: $status", None)

      case None => Result(JenkinsBuildNotFound, "Build Not Found: Never been built", None)
    }
}
