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

package uk.gov.hmrc.healthindicators.raters.leakdetection

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.ScoreConfig

class LeakDetectionRatingSpec extends AnyWordSpec with Matchers {

  val scoreConfig          = new ScoreConfig
  val leakDetectionRating0 = new LeakDetectionRating(0)
  val leakDetectionRating1 = new LeakDetectionRating(1)
  val leakDetectionRating2 = new LeakDetectionRating(2)
  val leakDetectionRating3 = new LeakDetectionRating(10)

  "calculate" should {

    "Return 0 when count is 0" in {
      leakDetectionRating0.calculateScore(scoreConfig) mustBe 0
    }

    "Match scoreConfig.leakDetection when count is 1" in {
      leakDetectionRating1.calculateScore(scoreConfig) mustBe scoreConfig.leakDetection
    }

    "Match scoreConfig.leakDetection * 2 when count is 2" in {
      leakDetectionRating2.calculateScore(scoreConfig) mustBe scoreConfig.leakDetection * 2
    }

    "Match scoreConfig.leakDetection*10 when count is 10" in {
      leakDetectionRating3.calculateScore(scoreConfig) mustBe scoreConfig.leakDetection * 10
    }
  }
}
