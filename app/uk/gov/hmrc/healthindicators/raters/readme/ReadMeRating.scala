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

package uk.gov.hmrc.healthindicators.raters.readme

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.models.Rating

case class ReadMeRating(
  length: Int,
  message: ReadMeResult
) extends Rating {
  override def ratingType: String  = "ReadMeRating"
  override def calculateScore: Int = ReadMeRating.calculate(this)
}

object ReadMeRating {
  def calculate(readMeRating: ReadMeRating): Int =
    readMeRating.message.score

  val format: OFormat[ReadMeRating] = {
    implicit val rmrF = ReadMeResult.format

    ((__ \ "length").format[Int]
      ~ (__ \ "message").format[ReadMeResult])(ReadMeRating.apply, unlift(ReadMeRating.unapply))
  }
}
