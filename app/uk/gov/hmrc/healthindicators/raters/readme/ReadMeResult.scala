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

import play.api.libs.json._

sealed trait ReadMeResult {
  def score: Int
}

case object NoReadMe extends ReadMeResult {
  override def score: Int = 0
}

case object DefaultReadMe extends ReadMeResult {
  override def score: Int = 0
}

case object ValidReadMe extends ReadMeResult {
  override def score: Int = 100
}

object ReadMeResult {
  val format: Format[ReadMeResult] with Object = new Format[ReadMeResult] {

    override def reads(json: JsValue): JsResult[ReadMeResult] = {
      val k = json.as[String]
      k match {
        case "NoReadMe"      => JsSuccess(NoReadMe)
        case "DefaultReadMe" => JsSuccess(DefaultReadMe)
        case "ValidReadMe"   => JsSuccess(ValidReadMe)
        case s               => JsError(s"Invalid Result: $s")
      }
    }

    override def writes(o: ReadMeResult): JsValue =
      o match {
        case NoReadMe      => Json.toJson("NoReadMe")
        case DefaultReadMe => Json.toJson("DefaultReadMe")
        case ValidReadMe   => Json.toJson("ValidReadMe")
      }
  }
}
