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

import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.Instant

case class DataPoint(
  timestamp: Instant,
  overallScore: Int
)

object DataPoint {
  val format: OFormat[DataPoint] =
    ( (__ \ "timestamp"   ).format[Instant]
    ~ (__ \ "overallScore").format[Int]
    )(DataPoint.apply, unlift(DataPoint.unapply))
}

case class HistoricIndicatorAPI(
  repoName  : String,
  dataPoints: Seq[DataPoint]
)

object HistoricIndicatorAPI {
  def fromHistoricIndicators(historicIndicator: Seq[HistoricIndicator]): Option[HistoricIndicatorAPI] =
    historicIndicator.foldLeft(Option.empty[HistoricIndicatorAPI]) {
      (api: Option[HistoricIndicatorAPI], h: HistoricIndicator) =>
        api match {
          case None    => Some(HistoricIndicatorAPI(h.repoName, Seq(DataPoint(h.timestamp, h.overallScore))))
          case Some(a) => Some(a.copy(dataPoints = a.dataPoints :+ DataPoint(h.timestamp, h.overallScore)))
        }
    }

  val format: OFormat[HistoricIndicatorAPI] = {
    implicit val dataFormat: Format[DataPoint] = DataPoint.format
    ( (__ \ "repoName"  ).format[String]
    ~ (__ \ "dataPoints").format[Seq[DataPoint]]
    )(HistoricIndicatorAPI.apply, unlift(HistoricIndicatorAPI.unapply))
  }
}

case class HistoricIndicator(
  repoName    : String,
  timestamp   : Instant,
  overallScore: Int
)

object HistoricIndicator {
  import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
  val mongoFormat: OFormat[HistoricIndicator] =
    ( (__ \ "repoName"    ).format[String]
    ~ (__ \ "timestamp"   ).format[Instant](MongoJavatimeFormats.instantFormat)
    ~ (__ \ "overallScore").format[Int]
    )(HistoricIndicator.apply, unlift(HistoricIndicator.unapply))
}
