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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json._

class IndicatorsSpec extends AnyWordSpec with Matchers {

  implicit val format: Format[ResultType] = ResultType.format

  "ResultType" should {

    "parse ValidReadMe" in {
      JsString("valid-readme").validate[ResultType] mustBe JsSuccess(ValidReadme)
    }

    "parse DefaultReadme" in {
      JsString("default-readme").validate[ResultType] mustBe JsSuccess(DefaultReadme)
    }

    "parse NoReadme" in {
      JsString("no-readme").validate[ResultType] mustBe JsSuccess(NoReadme)
    }

    "parse LeakDetectionViolation" in {
      JsString("leak-detection-violation").validate[ResultType] mustBe JsSuccess(LeakDetectionViolation)
    }

    "parse BobbyRulePending" in {
      JsString("bobby-rule-pending").validate[ResultType] mustBe JsSuccess(BobbyRulePending)
    }

    "parse BobbyRuleActive" in {
      JsString("bobby-rule-active").validate[ResultType] mustBe JsSuccess(BobbyRuleActive)
    }

    "error on unrecognised ResultType" in {

      val exception = intercept[JsResultException] {
        JsString("bobby-rule").as[ResultType]
      }
      exception.getMessage must include("Invalid Result Type")
    }

    "write ValidReadme" in {
      Json.toJson(ValidReadme) mustBe JsString("valid-readme")
    }

    "write DefaultReadme" in {
      Json.toJson(DefaultReadme) mustBe JsString("default-readme")
    }

    "write NoReadme" in {
      Json.toJson(NoReadme) mustBe JsString("no-readme")
    }

    "write LeakDetectionViolation" in {
      Json.toJson(LeakDetectionViolation) mustBe JsString("leak-detection-violation")
    }

    "write BobbyRulePending" in {
      Json.toJson(BobbyRulePending) mustBe JsString("bobby-rule-pending")
    }

    "write BobbyRuleActive" in {
      Json.toJson(BobbyRuleActive) mustBe JsString("bobby-rule-active")
    }
  }
}
