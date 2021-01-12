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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.models.{Rating, RatingType}
import uk.gov.hmrc.healthindicators.configs.ScoreConfig

case class BobbyRulesRating (
      pendingViolations: Int,
      activeViolations: Int,
  ) extends Rating {
    override def ratingType: RatingType = RatingType.BobbyRules
    override def calculateScore(scoreConfig: ScoreConfig): Int = {
        scoreConfig.bobbyRuleActive * activeViolations +
            scoreConfig.bobbyRulePending * pendingViolations
    }

    override val reason: String =
        s"You Scored this because you have $pendingViolations pending and $activeViolations active violations"
}

object BobbyRulesRating {
    val format: OFormat[BobbyRulesRating] =
        ((__ \ "pendingViolations").format[Int]
            ~ (__ \ "activeViolations").format[Int])(BobbyRulesRating.apply, unlift(BobbyRulesRating.unapply))
}

