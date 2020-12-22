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

package uk.gov.hmrc.healthindicators.raters.bobbyrules

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

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class BobbyRulesRaterSpec extends AnyWordSpec with Matchers with MockitoSugar {

    val mockBobbyRulesConnector = mock[BobbyRuleConnector]
    val rater                      = new BobbyRulesRater(mockBobbyRulesConnector)

    val dependenciesWithViolation = Dependencies(Seq(BobbyRuleViolations("reason", "from", "range")), "name")
    val dependenciesWithoutViolation = Dependencies(Seq(), "name")


    implicit val hc = HeaderCarrier()

    "BobbyRulesRater" should {

        "Return BobbyRulesRating Object with 100 Rating when no Report is found" in {
            when(mockBobbyRulesConnector.findLatestMasterReport("foo")) thenReturn Future.successful(None)

            val result = rater.countViolationsForRepo("foo")

            Await.result(result, 5 seconds) mustBe BobbyRulesRating(0)
        }

        "Return LeakDetectionRating Object with 100 Rating when a Report with 0 Results is found" in {
            when(mockBobbyRulesConnector.findLatestMasterReport("foo")) thenReturn Future.successful(
                Some(Report("repoName", Seq(), Seq(), Seq())))

            val result = rater.countViolationsForRepo("foo")

            Await.result(result, 5 seconds) mustBe BobbyRulesRating(0)
        }

        "Return LeakDetectionRating Object with 50 Rating when a Report with 1 Violation is found" in {
            when(mockBobbyRulesConnector.findLatestMasterReport("foo")) thenReturn Future.successful(
                Some(Report("repoName",
                    Seq(dependenciesWithViolation),
                    Seq(dependenciesWithoutViolation),
                    Seq(dependenciesWithoutViolation))))

            val result = rater.countViolationsForRepo("foo")

            Await.result(result, 5 seconds) mustBe BobbyRulesRating(1)
        }

        "Return LeakDetectionRating Object with 0 Rating when a Report with 2+ Results is found" in {
            when(mockBobbyRulesConnector.findLatestMasterReport("foo")) thenReturn Future.successful(
                Some(Report("repoName",
                    Seq(dependenciesWithViolation),
                    Seq(dependenciesWithViolation),
                    Seq(dependenciesWithViolation))))

            val result = rater.countViolationsForRepo("foo")

            Await.result(result, 5 seconds) mustBe BobbyRulesRating(3)
        }
    }
}

