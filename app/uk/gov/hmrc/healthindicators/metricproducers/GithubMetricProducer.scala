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
import uk.gov.hmrc.healthindicators.connectors.{GithubConnector, OpenPR}
import uk.gov.hmrc.healthindicators.models.{CleanGithub, DefaultReadme, GithubMetricType, Metric, NoReadme, Result, StalePR}

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GithubMetricProducer @Inject() (
  githubConnector: GithubConnector
)(implicit
  val ec: ExecutionContext
) extends MetricProducer {

  private val logger: Logger = Logger(this.getClass)

  val prStalenessDays = 30

  override def produce(repo: String): Future[Metric] = {
    logger.debug(s"Metric ReadMe for: $repo")

    for {
      readMe <- githubConnector.findReadMe(repo).map(getReadMeResultType)
      openPR <- githubConnector.getOpenPRs(repo).map(getPRResultType)
      groupStalePR = openPR.headOption.map(_.copy(description = s"Found ${openPR.length} Stale PR's"))
      result       = readMe ++ groupStalePR
      clean =
        if (result.isEmpty) Seq(Result(CleanGithub, "Clean GitHub: Valid ReadMe and no Stale PRS", None)) else Seq.empty
    } yield Metric(GithubMetricType, result ++ clean)

  }

  def getPRResultType(maybeOpenPRs: Option[Seq[OpenPR]]): Seq[Result] =
    maybeOpenPRs match {
      case None          => Seq.empty
      case Some(openPRs) => findStalePRs(openPRs)
    }

  def findStalePRs(openPRs: Seq[OpenPR]): Seq[Result] =
    openPRs.flatMap(isStale) match {
      case Seq()                => Seq.empty
      case results: Seq[Result] => results
    }

  def isStale(openPR: OpenPR): Option[Result] =
    if (ChronoUnit.DAYS.between(openPR.updatedAt, LocalDate.now()) > prStalenessDays)
      Some(Result(StalePR, s"${openPR.title}: PR older than $prStalenessDays days", None))
    else
      None

  private def getReadMeResultType(readme: Option[String]): Seq[Result] =
    readme match {
      case Some(str) if str.contains("This is a placeholder README.md for a new repository") =>
        Seq(Result(DefaultReadme, "Default readme", None))
      case None    => Seq(Result(NoReadme, "No Readme defined", None))
      case Some(_) => Seq.empty
    }
}
