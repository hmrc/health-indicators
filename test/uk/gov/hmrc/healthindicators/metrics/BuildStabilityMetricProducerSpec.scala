/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.healthindicators.metrics

import java.time.{Duration, Instant}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{JenkinsBuildReport, JenkinsBuildStatus, JenkinsConnector, JenkinsUrl, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.models.{BuildStabilityMetricType, JenkinsBuildNotFound, JenkinsBuildOutdated, JenkinsBuildStable, JenkinsBuildUnstable, Result}
import uk.gov.hmrc.healthindicators.metricproducers.BuildStabilityMetricProducer
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BuildStabilityMetricProducerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with ArgumentMatchersSugar {

  private val mockTeamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
  private val mockJenkinsConnector: JenkinsConnector                           = mock[JenkinsConnector]
  private val rater: BuildStabilityMetricProducer =
    new BuildStabilityMetricProducer(mockJenkinsConnector, mockTeamsAndRepositoriesConnector)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "BuildStabilityMetricProducer.produce" should {

    "return a Metric with JenkinsBuildNotFound result when no build is found" in {
      when(mockTeamsAndRepositoriesConnector.getJenkinsUrl(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))
      when(mockJenkinsConnector.getBuildJob("foo")).thenReturn(Future.successful(Some(JenkinsBuildReport(None))))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe BuildStabilityMetricType
      result.results.head.resultType mustBe JenkinsBuildNotFound
    }

    "return a Metric with JenkinsBuildNotFound result when no jenkins job matches URL" in {
      val url = new JenkinsUrl("foo/123")

      when(mockTeamsAndRepositoriesConnector.getJenkinsUrl(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(url)))
      when(mockJenkinsConnector.getBuildJob(url.jenkinsURL)).thenReturn(Future.successful(None))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe BuildStabilityMetricType
      result.results.head.resultType mustBe JenkinsBuildNotFound
    }

    "return a Metric with JenkinsBuildStable result when build is found" in {
      val url         = new JenkinsUrl("foo/123")
      val buildReport = JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.now())))

      when(mockTeamsAndRepositoriesConnector.getJenkinsUrl(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(url)))
      when(mockJenkinsConnector.getBuildJob(url.jenkinsURL)).thenReturn(Future.successful(Some(buildReport)))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe BuildStabilityMetricType
      result.results.head.resultType mustBe JenkinsBuildStable
    }
  }

  "BuildStabilityMetricProducer.getResultType" should {
    "be stable if last build was successful" in {
      val jenkinsBuildStable =
        JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.now())))

      BuildStabilityMetricProducer
        .getResultType(jenkinsBuildStable) mustBe Result(JenkinsBuildStable, "Build Stable: Everything is good", None)

    }

    "be outdated and warn if not built in the last 300 days" in {
      val jenkinsBuildOutdated =
        JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.now().minus(Duration.ofDays(301)))))

      BuildStabilityMetricProducer.getResultType(jenkinsBuildOutdated) mustBe Result(
        JenkinsBuildOutdated,
        "Build Outdated: Not been built in the last 300 days",
        None
      )

    }

    "be stable when build failed but only in the last 2 days" in {
      val jenkinsBuildStable =
        JenkinsBuildReport(Some(JenkinsBuildStatus("FAILURE", Instant.now().minus(Duration.ofDays(1)))))

      BuildStabilityMetricProducer.getResultType(jenkinsBuildStable) mustBe Result(
        JenkinsBuildStable,
        "Build Stable: Build recently failed",
        None
      )

    }

    "be unstable when build has been broken for more than 2 days" in {
      val jenkinsBuildUnstable =
        JenkinsBuildReport(Some(JenkinsBuildStatus("FAILURE", Instant.now().minus(Duration.ofDays(3)))))

      BuildStabilityMetricProducer.getResultType(jenkinsBuildUnstable) mustBe Result(
        JenkinsBuildUnstable,
        "Build Unstable: has been broken for more than 2 days",
        None
      )

    }

    "build is not found when there is a unknown status" in {
      val jenkinsBuildNotFound =
        JenkinsBuildReport(Some(JenkinsBuildStatus("UNKNOWN STATUS", Instant.now().minus(Duration.ofDays(3)))))

      BuildStabilityMetricProducer
        .getResultType(jenkinsBuildNotFound) mustBe Result(JenkinsBuildNotFound, "Unknown Status: UNKNOWN STATUS", None)

    }

    "build is not found when it has never been built" in {
      val jenkinsBuildNotFound =
        JenkinsBuildReport(None)

      BuildStabilityMetricProducer.getResultType(jenkinsBuildNotFound) mustBe Result(
        JenkinsBuildNotFound,
        "Build Not Found: Never been built",
        None
      )

    }
  }
}
