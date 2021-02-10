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
