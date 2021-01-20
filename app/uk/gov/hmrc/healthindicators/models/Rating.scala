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

package uk.gov.hmrc.healthindicators.models

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsString, Writes, __}

case object ReadMe extends RatingType

case object LeakDetection extends RatingType

case object BobbyRule extends RatingType

sealed trait RatingType

object RatingType {
  val writes: Writes[RatingType] = {
    case ReadMe        => JsString("ReadMe")
    case LeakDetection => JsString("LeakDetection")
    case BobbyRule     => JsString("BobbyRule")
  }
}

case class Score(points: Int, description: String, href: Option[String])

object Score {
  val writes: Writes[Score] =
    ((__ \ "points").write[Int]
      ~ (__ \ "description").write[String]
      ~ (__ \ "ratings").writeNullable[String])(unlift(Score.unapply))
}

case class Rating(ratingType: RatingType, ratingScore: Int, breakdown: Seq[Score])

object Rating {
  val writes: Writes[Rating] = {
    implicit val sW: Writes[Score]       = Score.writes
    implicit val rtW: Writes[RatingType] = RatingType.writes
    ((__ \ "ratingType").write[RatingType]
      ~ (__ \ "ratingScore").write[Int]
      ~ (__ \ "breakdown").write[Seq[Score]])(unlift(Rating.unapply))
  }
}

case class RepositoryRating(repositoryName: String, repositoryScore: Int, ratings: Option[Seq[Rating]])

object RepositoryRating {
  val writes: Writes[RepositoryRating] = {
    implicit val rW: Writes[Rating] = Rating.writes
    ((__ \ "repositoryName").write[String]
      ~ (__ \ "repositoryScore").write[Int]
      ~ (__ \ "ratings").writeNullable[Seq[Rating]])(unlift(RepositoryRating.unapply))
  }
}
