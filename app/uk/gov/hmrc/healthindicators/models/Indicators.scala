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
import uk.gov.hmrc.healthindicators.connectors.RepositoryType
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

sealed trait ResultType

sealed trait ReadMeResultType extends ResultType

case object ValidReadme extends ReadMeResultType {
  override def toString: String = "valid-readme"
}
case object DefaultReadme extends ReadMeResultType {
  override def toString: String = "default-readme"
}
case object NoReadme extends ReadMeResultType {
  override def toString: String = "no-readme"
}

sealed trait LeakDetectionResultType extends ResultType

case object LeakDetectionViolation extends LeakDetectionResultType {
  override def toString: String = "leak-detection-violation"
}

sealed trait BobbyRuleResultType extends ResultType

case object BobbyRulePending extends BobbyRuleResultType {
  override def toString: String = "bobby-rule-pending"
}
case object BobbyRuleActive extends BobbyRuleResultType {
  override def toString: String = "bobby-rule-active"
}

sealed trait JenkinsResultType extends ResultType

case object JenkinsBuildStable extends JenkinsResultType {
  override def toString: String = "jenkins-build-stable"
}
case object JenkinsBuildUnstable extends JenkinsResultType {
  override def toString: String = "jenkins-build-unstable"
}
case object JenkinsBuildNotFound extends JenkinsResultType {
  override def toString: String = "jenkins-build-not-found"
}
case object JenkinsBuildOutdated extends JenkinsResultType {
  override def toString: String = "jenkins-build-outdated"
}

sealed trait AlertConfigResultType extends ResultType

case object AlertConfigEnabled extends AlertConfigResultType {
  override def toString: String = "alert-config-enabled"
}
case object AlertConfigDisabled extends AlertConfigResultType {
  override def toString: String = "alert-config-disabled"
}
case object AlertConfigNotFound extends AlertConfigResultType {
  override def toString: String = "alert-config-not-found"
}

object ResultType {

  private val resultTypes = Set(
    ValidReadme,
    DefaultReadme,
    NoReadme,
    LeakDetectionViolation,
    BobbyRulePending,
    BobbyRuleActive,
    JenkinsBuildStable,
    JenkinsBuildUnstable,
    JenkinsBuildNotFound,
    JenkinsBuildOutdated,
    AlertConfigEnabled,
    AlertConfigDisabled,
    AlertConfigNotFound
  )

  def apply(value: String): Option[ResultType] = resultTypes.find(_.toString == value)

  val format: Format[ResultType] = new Format[ResultType] {
    override def reads(json: JsValue): JsResult[ResultType] =
      json.validate[String].flatMap { str =>
        ResultType(str).fold[JsResult[ResultType]](JsError(s"Invalid Result Type: $str"))(JsSuccess(_))
      }

    override def writes(o: ResultType): JsValue = JsString(o.toString)
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

case object ReadMeIndicatorType extends IndicatorType {
  override def toString: String = "read-me-indicator"
}

case object LeakDetectionIndicatorType extends IndicatorType {
  override def toString: String = "leak-detection-indicator"
}

case object BobbyRuleIndicatorType extends IndicatorType {
  override def toString: String = "bobby-rule-indicator"
}

case object BuildStabilityIndicatorType extends IndicatorType {
  override def toString: String = "build-stability-indicator"
}

case object AlertConfigIndicatorType extends IndicatorType {
  override def toString: String = "alert-config-indicator"
}

object IndicatorType {

  private val indicatorTypes =
    Set(ReadMeIndicatorType, LeakDetectionIndicatorType, BobbyRuleIndicatorType, BuildStabilityIndicatorType, AlertConfigIndicatorType)

  def apply(value: String): Option[IndicatorType] = indicatorTypes.find(_.toString == value)

  val format: Format[IndicatorType] = new Format[IndicatorType] {
    override def reads(json: JsValue): JsResult[IndicatorType] =
      json.validate[String].flatMap { str =>
        IndicatorType(str).fold[JsResult[IndicatorType]](JsError(s"Invalid Indicator: $str"))(JsSuccess(_))
      }

    override def writes(o: IndicatorType): JsValue = JsString(o.toString)
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

case class RepositoryHealthIndicator(
  repositoryName: String,
  timestamp: Instant,
  repositoryType: RepositoryType,
  indicators: Seq[Indicator]
)

object RepositoryHealthIndicator {
  val mongoFormats: OFormat[RepositoryHealthIndicator] = {
    implicit val instantFormat: Format[Instant]               = MongoJavatimeFormats.instantFormats
    implicit val indicatorFormat: Format[Indicator]           = Indicator.format
    implicit val repositoryTypeFormat: Format[RepositoryType] = RepositoryType.format
    ((__ \ "repositoryName").format[String]
      ~ (__ \ "timestamp").format[Instant]
      ~ (__ \ "repositoryType").format[RepositoryType]
      ~ (__ \ "indicators")
        .format[Seq[Indicator]])(RepositoryHealthIndicator.apply, unlift(RepositoryHealthIndicator.unapply))
  }
}
