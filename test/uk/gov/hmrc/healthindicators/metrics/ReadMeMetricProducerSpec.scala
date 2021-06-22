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
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.GithubConnector
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReadMeMetricProducerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockGithubConnector: GithubConnector = mock[GithubConnector]
  private val rater: ReadMeMetricProducer                   = new ReadMeMetricProducer(mockGithubConnector)

  private val readMeDeafult = "This is a placeholder README.md for a new repository"
  private val readMeValid   = "This is a valid README.md"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "rate" should {

    "Return Indicator with NoReadme result when no readme found" in {
      when(mockGithubConnector.findReadMe("foo")).thenReturn(Future.successful(None))

      val result = rater.produce("foo")

      result.futureValue mustBe Metric(ReadMeMetricType, Seq(Result(NoReadme, "No Readme defined", None)))
    }

    "Return Indicator with DefaultReadme result when default readme found" in {
      when(mockGithubConnector.findReadMe("foo")).thenReturn(Future.successful(Some(readMeDeafult)))

      val result = rater.produce("foo")

      result.futureValue mustBe Metric(ReadMeMetricType, Seq(Result(DefaultReadme, "Default readme", None)))
    }

    "Return Indicator with ValidReadme result when readme has been defined" in {
      when(mockGithubConnector.findReadMe("foo")).thenReturn(Future.successful(Some(readMeValid)))

      val result = rater.produce("foo")

      result.futureValue mustBe Metric(ReadMeMetricType, Seq(Result(ValidReadme, "Valid readme", None)))
    }
  }
}
