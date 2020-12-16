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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BobbyRulesRatingSpec extends AnyWordSpec with Matchers {

    val bobbyRulesRating0 = new BobbyRulesRating(0)
    val bobbyRulesRating1 = new BobbyRulesRating(1)
    val bobbyRulesRating2 = new BobbyRulesRating(2)
    val bobbyRulesRating3 = new BobbyRulesRating(3)

    "calculate" should {

        "Return 100 when count is 0" in {
            BobbyRulesRating.calculate(bobbyRulesRating0) mustBe 100
        }

        "Return 50 when count is 1" in {
            BobbyRulesRating.calculate(bobbyRulesRating1) mustBe 50
        }

        "Return 0 when count is 2" in {
            BobbyRulesRating.calculate(bobbyRulesRating2) mustBe 0
        }

        "Return 0 when count is more than 2" in {
            BobbyRulesRating.calculate(bobbyRulesRating3) mustBe 0
        }
    }
}