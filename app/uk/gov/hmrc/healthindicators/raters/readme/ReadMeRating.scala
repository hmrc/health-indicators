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

package uk.gov.hmrc.healthindicators.raters.readme

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.models.{Rating, RatingType}
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeType.{DefaultReadMe, NoReadMe, ValidReadMe}

sealed trait ReadMeType { def asString: String }

object ReadMeType {

  case object NoReadMe extends ReadMeType {
    def asString = "NoReadMe"
  }

  case object DefaultReadMe extends ReadMeType {
    def asString = "DefaultReadMe"
  }

  case object ValidReadMe extends ReadMeType {
    def asString = "ValidReadMe"
  }

  private val values = Seq(NoReadMe, DefaultReadMe, ValidReadMe)

  val format: Format[ReadMeType] = new Format[ReadMeType] {

    override def reads(json: JsValue): JsResult[ReadMeType] =
      json.validate[String].flatMap { s =>
        values
          .find(_.asString == s)
          .fold[JsResult[ReadMeType]](JsError(s"Invalid Result: $s"))(v => JsSuccess(v))
      }

    override def writes(o: ReadMeType): JsValue =
      Json.toJson(o.asString)
  }
}

case class ReadMeRating(
  readMeType: ReadMeType
) extends Rating {
  override def ratingType: RatingType = RatingType.ReadMe
  override val reason: String         = s"You scored this because you have a " + this.readMeType.toString
  override def calculateScore(scoreConfig: ScoreConfig): Int =
    readMeType match {
      case NoReadMe      => 0
      case DefaultReadMe => 0
      case ValidReadMe   => 100
    }
}

object ReadMeRating {
  val format: OFormat[ReadMeRating] = {
    implicit val rmtF = ReadMeType.format
    (__ \ "readMeType").format[ReadMeType].inmap(ReadMeRating.apply, unlift(ReadMeRating.unapply))
  }
}
