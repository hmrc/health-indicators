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

package uk.gov.hmrc.healthindicators.configs

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import play.api.libs.json.{JsError, Json, __}
import uk.gov.hmrc.healthindicators.models.RatingType

class WeightsConfigSpec extends AnyWordSpec with Matchers {

  "weightsLookup" should {

    "Read weights from config file as Map[String, Double]" in {

      val weightsConfig =
        new WeightsConfig(Configuration("weights.config.path" -> "/config/test-config.json"))

      weightsConfig.weightsLookup mustBe Map(RatingType.ReadMe -> 1.0, RatingType.LeakDetection -> 2.0)
    }

    implicit val wF = WeightsConfig.reads

    "Return a JsError if trying to parse an invalid Double in Map" in {

      Json
        .parse(
          """
          {
            "ReadMeRating": 1.0,
            "LeakDetectionRating": "Test"
          }
          """
        )
        .validate[Map[RatingType, Double]] mustBe JsError(__ \ "LeakDetectionRating", "error.expected.jsnumber")
    }

    "Return a JsError if trying to parse an invalid Key in Map" in {

      Json
        .parse(
          """
          {
            "Test": 1.0,
            "LeakDetectionRating": 2.0
          }
          """
        )
        .validate[Map[RatingType, Double]] mustBe JsError(__, "Invalid Rating: Test")
    }
  }
}
