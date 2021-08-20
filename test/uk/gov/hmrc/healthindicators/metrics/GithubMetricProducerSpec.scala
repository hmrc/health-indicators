/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.MockitoSugar
import org.mockito.ArgumentMatchersSugar.eqTo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{GithubConnector, OpenPR}
import uk.gov.hmrc.healthindicators.metricproducers.GithubMetricProducer
import uk.gov.hmrc.healthindicators.models.{CleanGithub, DefaultReadme, GithubMetricType, Metric, NoReadme, Result, StalePR}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GithubMetricProducerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  private val mockGithubConnector: GithubConnector = mock[GithubConnector]
  private val producer: GithubMetricProducer = new GithubMetricProducer(mockGithubConnector)

  private val readMeDefault = "This is a placeholder README.md for a new repository"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = reset(mockGithubConnector)

  "GithubMetricProducer.produce" should {

    "Return a Metric with NoReadMe when no readme found" in {
      when(mockGithubConnector.findReadMe(eqTo("foo"))).thenReturn(Future.successful(None))
      when(mockGithubConnector.getOpenPRs(eqTo("foo"))).thenReturn(Future.successful(None))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(GithubMetricType, Seq(Result(NoReadme, "No Readme defined", None)))
    }

    "Return a Metric with DefaultReadMe when default readme is found" in {
      when(mockGithubConnector.findReadMe(eqTo("foo"))).thenReturn(Future.successful(Some(readMeDefault)))
      when(mockGithubConnector.getOpenPRs(eqTo("foo"))).thenReturn(Future.successful(None))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(GithubMetricType, Seq(Result(DefaultReadme, "Default readme", None)))
    }

    "Return a Metric with StalePR when there is an Open PR over 30 days old" in {
      when(mockGithubConnector.findReadMe(eqTo("foo"))).thenReturn(Future.successful(Some("This is a valid readme")))
      when(mockGithubConnector.getOpenPRs(eqTo("foo"))).thenReturn(
        Future.successful(
          Some(
            Seq(
              OpenPR(
                "hello-world",
                LocalDate.parse("2021-04-16T13:38:36Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDate.now.minusDays(31L)
              )
            )
          )
        ))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(GithubMetricType, Seq(Result(StalePR, "Found 1 Stale PR's", None)))
    }

    "Return a Metric with CleanGithub when there is valid read me and no stale PRs" in {
      when(mockGithubConnector.findReadMe(eqTo("foo"))).thenReturn(Future.successful(Some("This is a valid readme")))
      when(mockGithubConnector.getOpenPRs(eqTo("foo"))).thenReturn(Future.successful(None))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(GithubMetricType, Seq(Result(CleanGithub, "Clean GitHub: Valid ReadMe and no Stale PRS", None)))
    }

    "Return a Metric with NoReadMe and StalePR" in {
      when(mockGithubConnector.findReadMe(eqTo("foo"))).thenReturn(Future.successful(None))
      when(mockGithubConnector.getOpenPRs(eqTo("foo"))).thenReturn(
        Future.successful(
          Some(
            Seq(
              OpenPR(
                "hello-world",
                LocalDate.parse("2021-04-16T13:38:36Z", DateTimeFormatter.ISO_DATE_TIME),
                LocalDate.now.minusDays(31L)
              )
            )
          )
        ))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(GithubMetricType, Seq(Result(NoReadme, "No Readme defined", None), Result(StalePR, s"Found 1 Stale PR's", None)))

    }
  }

  "GithubMetricProducer.isStale" should {
    "Return None when is less than 30 days old" in {
      val pr = OpenPR(
        title = "hello-world",
        createdAt = LocalDate.parse("2021-04-16T13:38:36Z", DateTimeFormatter.ISO_DATE_TIME),
        updatedAt = LocalDate.now.minusDays(1L)
      )
      producer.isStale(pr) mustBe None
    }

    "Return StalePR Metric when is more than 30 days old" in {
      val pr = OpenPR(
        title = "hello-world",
        createdAt = LocalDate.parse("2021-04-16T13:38:36Z", DateTimeFormatter.ISO_DATE_TIME),
        updatedAt = LocalDate.now.minusDays(31L)
      )
      producer.isStale(pr) mustBe Some(Result(StalePR, s"${pr.title}: PR older than ${producer.prStalenessDays} days", None))
    }
  }

}
