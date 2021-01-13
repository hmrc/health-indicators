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

import java.time.Instant
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RepoRatings(
  repo: String,
  date: Instant,
  ratings: Seq[Rating]
)

object RepoRatings {
  val mongoFormats: OFormat[RepoRatings] = {
    implicit val rF: Format[Rating]  = Rating.format
    implicit val iF: Format[Instant] = MongoJavatimeFormats.instantFormats

    ((__ \ "repo").format[String]
      ~ (__ \ "date").format[Instant]
      ~ (__ \ "ratings").format[Seq[Rating]])(RepoRatings.apply, unlift(RepoRatings.unapply))
  }
}
