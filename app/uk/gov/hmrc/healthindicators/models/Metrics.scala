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

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.connectors.RepoType
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

sealed trait ResultType

sealed trait OpenPRResultType extends ResultType

case object PRsNotFound extends OpenPRResultType {
  override val toString: String = "prs-not-found"
}

case object NoStalePRs extends OpenPRResultType {
  override val toString: String = "no-stale-prs"
}

case object StalePR extends OpenPRResultType {
  override val toString: String = "stale-pr"
}

sealed trait ReadMeResultType extends ResultType

case object ValidReadme extends ReadMeResultType {
  override val toString: String = "valid-readme"
}
case object DefaultReadme extends ReadMeResultType {
  override val toString: String = "default-readme"
}
case object NoReadme extends ReadMeResultType {
  override val toString: String = "no-readme"
}

sealed trait LeakDetectionResultType extends ResultType

case object LeakDetectionViolation extends LeakDetectionResultType {
  override val toString: String = "leak-detection-violation"
}

sealed trait BobbyRuleResultType extends ResultType

case object BobbyRulePending extends BobbyRuleResultType {
  override val toString: String = "bobby-rule-pending"
}
case object BobbyRuleActive extends BobbyRuleResultType {
  override val toString: String = "bobby-rule-active"
}

sealed trait JenkinsResultType extends ResultType

case object JenkinsBuildStable extends JenkinsResultType {
  override val toString: String = "jenkins-build-stable"
}
case object JenkinsBuildUnstable extends JenkinsResultType {
  override val toString: String = "jenkins-build-unstable"
}
case object JenkinsBuildNotFound extends JenkinsResultType {
  override val toString: String = "jenkins-build-not-found"
}
case object JenkinsBuildOutdated extends JenkinsResultType {
  override val toString: String = "jenkins-build-outdated"
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
    PRsNotFound,
    NoStalePRs,
    StalePR,
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

sealed trait MetricType

case object OpenPRMetricType extends MetricType {
  override def toString: String = "open-pr"
}

case object ReadMeMetricType extends MetricType {
  override def toString: String = "read-me"
}

case object LeakDetectionMetricType extends MetricType {
  override def toString: String = "leak-detection"
}

case object BobbyRuleMetricType extends MetricType {
  override def toString: String = "bobby-rule"
}

case object BuildStabilityMetricType extends MetricType {
  override def toString: String = "build-stability"
}

case object AlertConfigMetricType extends MetricType {
  override def toString: String = "alert-config"
}

object MetricType {

  private val metricTypes =
    Set(
      ReadMeMetricType,
      LeakDetectionMetricType,
      BobbyRuleMetricType,
      BuildStabilityMetricType,
      AlertConfigMetricType,
      OpenPRMetricType
    )

  def apply(value: String): Option[MetricType] = metricTypes.find(_.toString == value)

  val format: Format[MetricType] = new Format[MetricType] {
    override def reads(json: JsValue): JsResult[MetricType] =
      json.validate[String].flatMap { str =>
        MetricType(str).fold[JsResult[MetricType]](JsError(s"Invalid Metric: $str"))(JsSuccess(_))
      }

    override def writes(o: MetricType): JsValue = JsString(o.toString)
  }
}

case class Metric(metricType: MetricType, results: Seq[Result])

object Metric {
  val format: OFormat[Metric] = {
    implicit val indicatorTypeFormat: Format[MetricType]    = MetricType.format
    implicit val resultFormat: Format[Result]               = Result.format
    ((__ \ "metricType").format[MetricType]
      ~ (__ \ "results").format[Seq[Result]])(Metric.apply, unlift(Metric.unapply))
  }
}

case class RepositoryMetrics(
                              repoName: String,
                              timestamp: Instant,
                              repoType: RepoType,
                              metrics: Seq[Metric]
)

object RepositoryMetrics {
  val mongoFormats: OFormat[RepositoryMetrics] = {
    implicit val instantFormat: Format[Instant]               = MongoJavatimeFormats.instantFormat
    implicit val indicatorFormat: Format[Metric]           = Metric.format
    implicit val repositoryTypeFormat: Format[RepoType] = RepoType.format
    ((__ \ "repoName").format[String]
      ~ (__ \ "timestamp").format[Instant]
      ~ (__ \ "repoType").format[RepoType]
      ~ (__ \ "metrics")
        .format[Seq[Metric]])(RepositoryMetrics.apply, unlift(RepositoryMetrics.unapply))
  }
}
