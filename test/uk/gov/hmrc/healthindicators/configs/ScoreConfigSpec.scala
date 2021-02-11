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

package uk.gov.hmrc.healthindicators.configs

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.{BobbyRuleActive, BobbyRulePending, DefaultReadme, LeakDetectionViolation, NoReadme, ValidReadme}

class ScoreConfigSpec extends AnyWordSpec with Matchers{
  "ScoreConfig" should {
    val scoreConfig = new ScoreConfig
    "Give correct scores for ReadMeResultTypes" in {
      scoreConfig.scores(NoReadme) shouldBe -50
      scoreConfig.scores(DefaultReadme) shouldBe -50
      scoreConfig.scores(ValidReadme) shouldBe 50
    }

    "give correct scores for LeakDetectionResultTypes" in {
      scoreConfig.scores(LeakDetectionViolation) shouldBe -50
    }

    "give correct scores for BobbyRulesResultTypes" in {
      scoreConfig.scores(BobbyRulePending) shouldBe -20
      scoreConfig.scores(BobbyRuleActive) shouldBe -100
    }
  }
}
