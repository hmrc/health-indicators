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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LeakDetectionRatingSpec extends AnyWordSpec with Matchers {

  val leakDetectionRating0 = new LeakDetectionRating(0)
  val leakDetectionRating1 = new LeakDetectionRating(1)
  val leakDetectionRating2 = new LeakDetectionRating(2)
  val leakDetectionRating3 = new LeakDetectionRating(3)

  "calculate" should {

    "Return 100 when count is 0" in {
      LeakDetectionRating.calculate(leakDetectionRating0) mustBe 100
    }

    "Return 50 when count is 1" in {
      LeakDetectionRating.calculate(leakDetectionRating1) mustBe 50
    }

    "Return 0 when count is 2" in {
      LeakDetectionRating.calculate(leakDetectionRating2) mustBe 0
    }

    "Reutn 0 when count is more than 2" in {
      LeakDetectionRating.calculate(leakDetectionRating3) mustBe 0
    }
  }
}
