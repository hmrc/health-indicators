/*
 * Copyright 2023 HM Revenue & Customs
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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class IndicatorsSpec extends AnyWordSpec with Matchers {

  implicit val format: Format[ResultType] = ResultType.format

  "ResultType" should {

    "parse DefaultReadme" in {
      JsString("default-readme").validate[ResultType] shouldBe JsSuccess(DefaultReadme)
    }

    "parse NoReadme" in {
      JsString("no-readme").validate[ResultType] shouldBe JsSuccess(NoReadme)
    }

    "write DefaultReadme" in {
      Json.toJson(DefaultReadme: ResultType) shouldBe JsString("default-readme")
    }

    "write NoReadme" in {
      Json.toJson(NoReadme: ResultType) shouldBe JsString("no-readme")
    }

    "write LeakDetectionViolation" in {
      Json.toJson(LeakDetectionViolation: ResultType) shouldBe JsString("leak-detection-violation")
    }
  }
}
