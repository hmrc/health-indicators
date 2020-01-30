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

package uk.gov.hmrc.healthindicators.model

import play.api.libs.json.Json

case class ReportLine(
                       filePath: String,
                       scope: String,
                       lineNumber: Int,
                       urlToSource: String,
                       ruleId: Option[String],
                       description: String,
                       lineText: String
)

case class Report(
                   _id: String,
                   inspectionResults: Seq[ReportLine]
)

object ReportLine {
  implicit val reportLineFormats = Json.format[ReportLine]

}

object Report {
  implicit val restDateTimeFormats  = uk.gov.hmrc.http.controllers.RestFormats.dateTimeFormats
  implicit val reportFormats = Json.format[Report]
}
