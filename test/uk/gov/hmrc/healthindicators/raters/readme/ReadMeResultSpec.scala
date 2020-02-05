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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json}

class ReadMeResultSpec extends AnyWordSpec with Matchers {

  implicit val f = ReadMeResult.format

  "ReadMeResult" should {
    "Should parse NoReadMe" in {
      JsString("NoReadMe").validate[ReadMeResult] mustBe JsSuccess(NoReadMe)
    }

    "Should parse DefaultReadMe" in {
      JsString("DefaultReadMe").validate[ReadMeResult] mustBe JsSuccess(DefaultReadMe)
    }

    "Should parse ValidReadMe" in {
      JsString("ValidReadMe").validate[ReadMeResult] mustBe JsSuccess(ValidReadMe)
    }

    "Should write NoReadMe" in {
      Json.toJson(NoReadMe) mustBe JsString("NoReadMe")
    }

    "Should write DefaultReadMe" in {
      Json.toJson(DefaultReadMe) mustBe JsString("DefaultReadMe")
    }

    "Should write ValidReadMe" in {
      Json.toJson(ValidReadMe) mustBe JsString("ValidReadMe")
    }
  }

}
