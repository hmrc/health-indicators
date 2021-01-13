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

package uk.gov.hmrc.healthindicators.raters.bobbyrules

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.ScoreConfig

class BobbyRulesRatingSpec extends AnyWordSpec with Matchers {
  val scoreConfig        = new ScoreConfig
  val bobbyRulesRating00 = new BobbyRulesRating(0, 0)
  val bobbyRulesRating10 = new BobbyRulesRating(1, 0)
  val bobbyRulesRating01 = new BobbyRulesRating(0, 1)
  val bobbyRulesRating40 = new BobbyRulesRating(4, 0)
  val bobbyRulesRating61 = new BobbyRulesRating(6, 1)

  "calculate" should {

    "Return 0 when no violations found" in {
      bobbyRulesRating00.calculateScore(scoreConfig) mustBe 0
    }

    "Match scoreConfig.bobbyRulePending when 1 pending and 0 active is found" in {
      bobbyRulesRating10.calculateScore(scoreConfig) mustBe scoreConfig.bobbyRulePending
    }

    "Match scoreConfig.bobbyRuleActive when 0 pending and 1 active is found" in {
      bobbyRulesRating01.calculateScore(scoreConfig) mustBe scoreConfig.bobbyRuleActive
    }

    "Match scoreConfig.bobbyRulePending * 4 when 4 pending and 0 active is found" in {
      bobbyRulesRating40.calculateScore(scoreConfig) mustBe scoreConfig.bobbyRulePending * 4
    }

    "Match scoreConfig.bobbyRulePending * 6 + scoreConfig.bobbyRuleActive when 6 pending and 1 active is found" in {
      bobbyRulesRating61.calculateScore(
        scoreConfig
      ) mustBe scoreConfig.bobbyRulePending * 6 + scoreConfig.bobbyRuleActive
    }
  }
}
