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
      count: Int
  ) extends Rating {
    override def ratingType: RatingType = RatingType.BobbyRules
    override def rating: Int    = BobbyRulesRating.calculate(this)
}

object BobbyRulesRating {
    def calculate(bobbyRulesRating: BobbyRulesRating): Int =
        bobbyRulesRating.count match {
            case 0 => 100
            case 1 => 50
            case _ => 0
        }

    val format: OFormat[BobbyRulesRating] =
        (__ \ "count").format[Int].inmap(BobbyRulesRating.apply, unlift(BobbyRulesRating.unapply))
}

