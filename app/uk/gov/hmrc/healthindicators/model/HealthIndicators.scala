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

package uk.gov.hmrc.healthindicators.model

import java.time.{LocalDateTime, ZoneOffset}

import play.api.libs.json._
import play.api.libs.functional.syntax._

trait Rating {
    def _type : String
    def rating: Int
}

case class ReadMeRating(
    rating: Int
  , length: Int
  , message: String
) extends Rating {
    override def _type: String = "ReadMeRating"
}

case class HealthIndicators(
    repo: String
  , date: LocalDateTime
  , ratings: Seq[Rating]
)

object Rating {
   val format: Format[Rating] with Object = new Format[Rating] {
      implicit val readMeRating = Json.format[ReadMeRating]

      override def reads(json: JsValue): JsResult[Rating] = {
         val k  = (json \ "_type").as[String]
         k match {
            case "ReadMeRating" => json.validate[ReadMeRating]
            case _ => ???
         }
      }

      override def writes(o: Rating): JsValue = {
         o match {
            case r: ReadMeRating => Json.toJsObject(r) + ("_type" -> Json.toJson("ReadMeRating"))
            case _ => ???
         }
      }
   }
}


object HealthIndicators {

   val localDateTimeRead: Reads[LocalDateTime] =
      __.read[Long].map { dateTime =>
        LocalDateTime.ofEpochSecond(dateTime, 0, ZoneOffset.UTC)
      }

   implicit val localDateTimeToEpochSecondsWrites: Writes[LocalDateTime] = new Writes[LocalDateTime] {
      def writes(dateTime: LocalDateTime): JsValue = JsNumber(value = dateTime.atOffset(ZoneOffset.UTC).toEpochSecond)
   }

   implicit val ratingFormat: Format[Rating] = Rating.format


   val mongoFormats: OFormat[HealthIndicators] =
      Json.format[HealthIndicators]
}

