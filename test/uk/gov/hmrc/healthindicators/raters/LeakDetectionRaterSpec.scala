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

package uk.gov.hmrc.healthindicators.raters

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{LeakDetectionConnector, Report, ReportLine}
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LeakDetectionRaterSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockLeakDetectionConnector: LeakDetectionConnector = mock[LeakDetectionConnector]
  private val rater: LeakDetectionRater                          = new LeakDetectionRater(mockLeakDetectionConnector)

  private val reportLine: ReportLine =
    ReportLine("file-path", "scope", 1, "url-to-source", Some("rule-id"), "description", "line-text")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "rate" should {

    "Return Indicator with no results when leak detection connector returns None" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo")).thenReturn(Future.successful(None))

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(LeakDetectionIndicatorType, Seq.empty)

    }

    "Return Indicator with no results when a Report with no violation is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo"))
        .thenReturn(Future.successful(Some(Report("idx", Seq.empty))))

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(LeakDetectionIndicatorType, Seq.empty)
    }

    "Return Indicator with a result when a Report with 1 violations is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo"))
        .thenReturn(Future.successful(Some(Report("idx", Seq(reportLine)))))

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(
        LeakDetectionIndicatorType,
        Seq(Result(LeakDetectionViolation, "description", Some("url-to-source")))
      )
    }

    "Return Indicator  with 2 results when Report with 2 violations is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo"))
        .thenReturn(Future.successful(Some(Report("idx", Seq(reportLine, reportLine)))))

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(
        LeakDetectionIndicatorType,
        Seq(
          Result(LeakDetectionViolation, "description", Some("url-to-source")),
          Result(LeakDetectionViolation, "description", Some("url-to-source"))
        )
      )
    }
  }
}
