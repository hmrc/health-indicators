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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

sealed trait ResultType

sealed trait ReadMeResultType extends ResultType

case object NoReadme extends ReadMeResultType

case object DefaultReadme extends ReadMeResultType

case object ValidReadme extends ReadMeResultType

sealed trait LeakDetectionResultType extends ResultType

case object LeakDetectionViolation extends LeakDetectionResultType

sealed trait BobbyRuleResultType extends ResultType

case object BobbyRulePending extends BobbyRuleResultType

case object BobbyRuleActive extends BobbyRuleResultType

object ResultType {
  val format: Format[ResultType] = new Format[ResultType] {
    override def reads(json: JsValue): JsResult[ResultType] =
      json.validate[String].flatMap {
        case "valid-readme"             => JsSuccess(ValidReadme)
        case "default-readme"           => JsSuccess(DefaultReadme)
        case "no-readme"                => JsSuccess(NoReadme)
        case "leak-detection-violation" => JsSuccess(LeakDetectionViolation)
        case "bobby-rule-pending"       => JsSuccess(BobbyRulePending)
        case "bobby-rule-active"        => JsSuccess(BobbyRuleActive)
        case s                          => JsError(s"Invalid Result Type: $s")
      }

    override def writes(o: ResultType): JsValue =
      o match {
        case ValidReadme            => JsString("valid-readme")
        case DefaultReadme          => JsString("default-readme")
        case NoReadme               => JsString("no-readme")
        case LeakDetectionViolation => JsString("leak-detection-violation")
        case BobbyRulePending       => JsString("bobby-rule-pending")
        case BobbyRuleActive        => JsString("bobby-rule-active")
      }
  }
}

case class Result(resultType: ResultType, description: String, href: Option[String])

object Result {
  val format: OFormat[Result] = {
    implicit val resultTypeFormat: Format[ResultType] = ResultType.format
    ((__ \ "resultType").format[ResultType]
      ~ (__ \ "description").format[String]
      ~ (__ \ "href").formatNullable[String])(Result.apply, unlift(Result.unapply))
  }
}

sealed trait IndicatorType

case object ReadMeIndicatorType extends IndicatorType

case object LeakDetectionIndicatorType extends IndicatorType

case object BobbyRuleIndicatorType extends IndicatorType

object IndicatorType {
  val format: Format[IndicatorType] = new Format[IndicatorType] {
    override def reads(json: JsValue): JsResult[IndicatorType] =
      json.validate[String].flatMap {
        case "read-me-indicator"        => JsSuccess(ReadMeIndicatorType)
        case "leak-detection-indicator" => JsSuccess(LeakDetectionIndicatorType)
        case "bobby-rule-indicator"     => JsSuccess(BobbyRuleIndicatorType)
        case s                          => JsError(s"Invalid Indicator: $s")
      }

    override def writes(o: IndicatorType): JsValue =
      o match {
        case ReadMeIndicatorType        => JsString("read-me-indicator")
        case LeakDetectionIndicatorType => JsString("leak-detection-indicator")
        case BobbyRuleIndicatorType     => JsString("bobby-rule-indicator")
        case s                          => JsString(s"$s")
      }
  }
}

case class Indicator(indicatorType: IndicatorType, results: Seq[Result])

object Indicator {
  val format: OFormat[Indicator] = {
    implicit val indicatorTypeFormat: Format[IndicatorType] = IndicatorType.format
    implicit val resultFormat: Format[Result]               = Result.format
    ((__ \ "indicatorType").format[IndicatorType]
      ~ (__ \ "results").format[Seq[Result]])(Indicator.apply, unlift(Indicator.unapply))
  }
}

case class RepositoryHealthIndicator(repositoryName: String, timestamp: Instant, indicators: Seq[Indicator])

object RepositoryHealthIndicator {
  val mongoFormats: OFormat[RepositoryHealthIndicator] = {
    implicit val instantFormat: Format[Instant]     = MongoJavatimeFormats.instantFormats
    implicit val indicatorFormat: Format[Indicator] = Indicator.format
    ((__ \ "repositoryName").format[String]
      ~ (__ \ "timestamp").format[Instant]
      ~ (__ \ "indicators")
        .format[Seq[Indicator]])(RepositoryHealthIndicator.apply, unlift(RepositoryHealthIndicator.unapply))
  }
}
