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

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{Writes, __}
import uk.gov.hmrc.healthindicators.connectors.RepoType

case class Breakdown(points: Int, description: String, href: Option[String])

object Breakdown {
  val writes: Writes[Breakdown] =
    ( (__ \ "points"     ).write[Int]
    ~ (__ \ "description").write[String]
    ~ (__ \ "link"       ).writeNullable[String]
    )(unlift(Breakdown.unapply))
}

case class WeightedMetric(
  metricType: MetricType,
  score     : Int,
  breakdown : Seq[Breakdown]
)

object WeightedMetric {
  val writes: Writes[WeightedMetric] = {
    implicit val sW: Writes[Breakdown]   = Breakdown.writes
    implicit val rtW: Writes[MetricType] = MetricType.format
    ( (__ \ "metricType").write[MetricType]
    ~ (__ \ "score"     ).write[Int]
    ~ (__ \ "breakdown" ).write[Seq[Breakdown]]
    )(unlift(WeightedMetric.unapply))
  }
}
case class Indicator(
  repoName       : String,
  repoType       : RepoType,
  overallScore   : Int,
  weightedMetrics: Seq[WeightedMetric]
)

object Indicator {
  val writes: Writes[Indicator] = {
    implicit val rW: Writes[WeightedMetric] = WeightedMetric.writes
    implicit val rtF: Writes[RepoType]      = RepoType.format
    ( (__ \ "repoName"       ).write[String]
    ~ (__ \ "repoType"       ).write[RepoType]
    ~ (__ \ "overallScore"   ).write[Int]
    ~ (__ \ "weightedMetrics").write[Seq[WeightedMetric]]
    )(unlift(Indicator.unapply))
  }
}
