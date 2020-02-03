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

package uk.gov.hmrc.healthindicators.raters.readme

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ReadMeRatingSpec extends AnyWordSpec with Matchers {

  val readMeRatingMissing = new ReadMeRating(0, "No README found")
  val readMeRatingDefault = new ReadMeRating(220, "Default README found")
  val readMeRatingValid   = new ReadMeRating(7265, "Valid README found")

  "calculate" should {

    "Return 0 when no README is found" in {
      ReadMeRating.calculate(readMeRatingMissing) mustBe 0
    }

    "Return 0 when default README is found" in {
      ReadMeRating.calculate(readMeRatingDefault) mustBe 0
    }

    "Return 100 when valid README is found" in {
      ReadMeRating.calculate(readMeRatingValid) mustBe 100
    }
  }
}
