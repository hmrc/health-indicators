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

case class BobbyRulesRating(
      pendingViolations: Int,
      activeViolations: Int
  ) extends Rating {
    override def ratingType: RatingType = RatingType.BobbyRules
    override def rating: Int    = BobbyRulesRating.calculate(this)
}

object BobbyRulesRating {
    def calculate(bobbyRulesRating: BobbyRulesRating): Int = {
//        bobbyRulesRating match {
//            case bobbyRulesRating.activeViolations == 0 && bobbyRulesRating.pendingViolations == 0 => 100
//            case bobbyRulesRating.activeViolations == 0 && bobbyRulesRating.pendingViolations == 1 => 75
//            case bobbyRulesRating.activeViolations == 0 && bobbyRulesRating.pendingViolations == 2 => 50
//            case bobbyRulesRating.activeViolations == 0 && bobbyRulesRating.pendingViolations == 3 => 25
//            case _ => 0

            val maxScore = 100
            val activeViolationScore = 100
            val pendingViolationScore = 20

            val score = maxScore-
                (activeViolationScore*bobbyRulesRating.activeViolations
                    + pendingViolationScore*bobbyRulesRating.pendingViolations)

            if(score<0) 0 else score
        }

    val format: OFormat[BobbyRulesRating] =
        //(__ \ "count").format[(Int, Int)].inmap(BobbyRulesRating.apply, unlift(BobbyRulesRating.unapply))
    ((__ \ "pendingViolations").format[Int]
        ~ (__ \ "activeViolations").format[Int])(BobbyRulesRating.apply, unlift(BobbyRulesRating.unapply))
}

