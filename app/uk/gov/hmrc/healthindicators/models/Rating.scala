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

import play.api.libs.json._
import uk.gov.hmrc.healthindicators.raters.bobbyrules.BobbyRulesRating
import uk.gov.hmrc.healthindicators.raters.leakdetection.LeakDetectionRating
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeRating

sealed trait RatingType { def asString: String }

object RatingType {
  case object ReadMe extends RatingType {
    def asString = "ReadMeRating"
  }
  case object LeakDetection extends RatingType {
    def asString = "LeakDetectionRating"
  }

  case object BobbyRules extends RatingType {
    def asString = "BobbyRulesRating"
  }

  private val values = Seq(ReadMe, LeakDetection, BobbyRules)

  def parse(s: String): Option[RatingType] =
    values
      .find(_.asString == s)

  val format: Format[RatingType] = new Format[RatingType] {

    override def reads(json: JsValue): JsResult[RatingType] =
      json.validate[String].flatMap { s =>
        parse(s)
          .fold[JsResult[RatingType]](JsError(s"Invalid Rating: $s"))(v => JsSuccess(v))
      }

    override def writes(o: RatingType): JsValue =
      Json.toJson(o.asString)
  }
}

trait Rating {
  def ratingType: RatingType
  def rating: Int
}

object Rating {

  val apiWrites: Writes[Rating] = (o: Rating) => {
    implicit val rtF = RatingType.format
    Json.obj("ratingType" -> o.ratingType, "rating" -> o.rating)
  }




  val format: Format[Rating] = new Format[Rating] {
    implicit val rtF = RatingType.format
    implicit val rmF = ReadMeRating.format
    implicit val ldF = LeakDetectionRating.format
    implicit val brF = BobbyRulesRating.format

    override def reads(json: JsValue): JsResult[Rating] =
      (json \ "type")
        .validate[RatingType]
        .flatMap {
          case RatingType.ReadMe        => json.validate[ReadMeRating]
          case RatingType.LeakDetection => json.validate[LeakDetectionRating]
          case RatingType.BobbyRules    => json.validate[BobbyRulesRating]
          case s                        => JsError(s"Invalid Rating: $s")
        }

    override def writes(o: Rating): JsValue =
      o match {
        case r: ReadMeRating        => Json.toJsObject(r) + ("type" -> Json.toJson(r.ratingType))
        case r: LeakDetectionRating => Json.toJsObject(r) + ("type" -> Json.toJson(r.ratingType))
        case r: BobbyRulesRating    => Json.toJsObject(r) + ("type" -> Json.toJson(r.ratingType))
      }
  }
}
