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

import java.time.LocalDateTime

import org.joda.time
import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.http.controllers.RestFormats

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

case class LeakDetectionRating(
    rating: Int
  , count: Int
) extends Rating {
   override def _type: String = "LeakDetectionRating"
}

case class HealthIndicators(
    repo: String
  , date: LocalDateTime
  , ratings: Seq[Rating]
)

object Rating {
   val format: Format[Rating] with Object = new Format[Rating] {
      implicit val readMeRating = Json.format[ReadMeRating]
      implicit val leakDetectionRating = Json.format[LeakDetectionRating]

      override def reads(json: JsValue): JsResult[Rating] = {
         val k  = (json \ "_type").as[String]
         k match {
            case "ReadMeRating" => json.validate[ReadMeRating]
            case "LeakDetectionRating" => json.validate[LeakDetectionRating]
            case _ => ???
         }
      }

      override def writes(o: Rating): JsValue = {
         o match {
            case r: ReadMeRating => Json.toJsObject(r) + ("_type" -> Json.toJson("ReadMeRating"))
            case r: LeakDetectionRating => Json.toJsObject(r) + ("_type" -> Json.toJson("LeakDetectionRating"))
            case _ => ???
         }
      }
   }
}


object HealthIndicators {

   implicit val ratingFormat: Format[Rating] = Rating.format
   implicit val localDateTimeFormat: Format[time.LocalDateTime] = RestFormats.localDateTimeFormats

   val mongoFormats: OFormat[HealthIndicators] =
      Json.format[HealthIndicators]
}
