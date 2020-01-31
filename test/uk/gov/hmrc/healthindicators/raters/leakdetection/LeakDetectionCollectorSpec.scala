/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.healthindicators.raters.leakdetection

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class LeakDetectionCollectorSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockLeakDetectionConnector = mock[LeakDetectionConnector]
  val rater = new LeakDetectionCollector(mockLeakDetectionConnector)

  val reportLine = ReportLine("file-path", "scope", 1, "url-to-source", Some("rule-id"), "description", "line-text")

  implicit val hc = HeaderCarrier()

  "LeakDetectionRater" should {

    "Return LeakDetectionRating Object with 100 Rating when no Report is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo")) thenReturn Future.successful(None)

      val result = rater.countLeakDetections("foo")

      Await.result(result, 5 seconds) mustBe LeakDetectionRating(0)
    }

    "Return LeakDetectionRating Object with 100 Rating when a Report with 0 Results is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo")) thenReturn Future.successful(Some(Report("idx", Seq.empty)))

      val result = rater.countLeakDetections("foo")

      Await.result(result, 5 seconds) mustBe LeakDetectionRating(0)
    }

    "Return LeakDetectionRating Object with 50 Rating when a Report with 1 Result is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo")) thenReturn Future.successful(Some(Report("idx", Seq(reportLine))))

      val result = rater.countLeakDetections("foo")

      Await.result(result, 5 seconds) mustBe LeakDetectionRating(1)
    }

    "Return LeakDetectionRating Object with 0 Rating when a Report with 2+ Results is found" in {
      when(mockLeakDetectionConnector.findLatestMasterReport("foo")) thenReturn Future.successful(Some(Report("idx", Seq(reportLine, reportLine, reportLine))))

      val result = rater.countLeakDetections("foo")

      Await.result(result, 5 seconds) mustBe LeakDetectionRating(3)
    }
  }
}
