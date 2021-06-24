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
import uk.gov.hmrc.healthindicators.models.{BobbyRuleActive, BobbyRulePending, DefaultReadme, JenkinsBuildNotFound, JenkinsBuildOutdated, JenkinsBuildStable, JenkinsBuildUnstable, LeakDetectionViolation, NoReadme, ValidReadme}

class PointsConfigSpec extends AnyWordSpec with Matchers {
  "PointsConfig" should {
    val scoreConfig = new PointsConfig
    "Give correct points for ReadMeResultTypes" in {
      scoreConfig.points(NoReadme)      shouldBe -50
      scoreConfig.points(DefaultReadme) shouldBe -50
      scoreConfig.points(ValidReadme)   shouldBe 50
    }

    "give correct points for LeakDetectionResultTypes" in {
      scoreConfig.points(LeakDetectionViolation) shouldBe -50
    }

    "give correct scores for BobbyRulesResultTypes" in {
      scoreConfig.points(BobbyRulePending) shouldBe -20
      scoreConfig.points(BobbyRuleActive)  shouldBe -100
    }

    "give correct points for BuildStabilityResultTypes" in {
      scoreConfig.points(JenkinsBuildStable)   shouldBe 50
      scoreConfig.points(JenkinsBuildUnstable) shouldBe -50
      scoreConfig.points(JenkinsBuildNotFound) shouldBe 0
      scoreConfig.points(JenkinsBuildOutdated) shouldBe -50
    }
  }
}
