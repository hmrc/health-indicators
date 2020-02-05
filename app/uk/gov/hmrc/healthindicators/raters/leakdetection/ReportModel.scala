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

package uk.gov.hmrc.healthindicators.raters.leakdetection

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ReportLine(
  filePath: String,
  scope: String,
  lineNumber: Int,
  urlToSource: String,
  ruleId: Option[String],
  description: String,
  lineText: String
)

object ReportLine {
  val reads: Reads[ReportLine] = {
    ((__ \ "filePath").read[String]
      ~ (__ \ "scope").read[String]
      ~ (__ \ "lineNumber").read[Int]
      ~ (__ \ "urlToSource").read[String]
      ~ (__ \ "ruleId").readNullable[String]
      ~ (__ \ "description").read[String]
      ~ (__ \ "lineText").read[String])(ReportLine.apply _)
  }
}

case class Report(
  reportId: String,
  inspectionResults: Seq[ReportLine]
)

object Report {
  val reads: Reads[Report] = {
    implicit val rlR = ReportLine.reads
    ((__ \ "_id").read[String]
      ~ (__ \ "inspectionResults").read[Seq[ReportLine]])(Report.apply _)
  }
}
