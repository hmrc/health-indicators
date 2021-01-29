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

package uk.gov.hmrc.healthindicators.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.healthindicators.configs.AppConfig
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeakDetectionConnector @Inject() (
  httpClient: HttpClient,
  healthIndicatorsConfig: AppConfig
)(implicit val ec: ExecutionContext) {

  //todo fix me
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val leakDetectionBaseUrl: String = healthIndicatorsConfig.leakDetectionUrl

  def findLatestMasterReport(repo: String): Future[Option[Report]] = {
    implicit val rF: Reads[Report] = Report.reads
    httpClient.GET[Option[Report]](s"$leakDetectionBaseUrl/api/reports/repositories/$repo")
  }
}

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
  reportId: String,
  inspectionResults: Seq[ReportLine]
)

object ReportLine {
  val reads: Reads[ReportLine] =
    ((__ \ "filePath").read[String]
      ~ (__ \ "scope").read[String]
      ~ (__ \ "lineNumber").read[Int]
      ~ (__ \ "urlToSource").read[String]
      ~ (__ \ "ruleId").readNullable[String]
      ~ (__ \ "description").read[String]
      ~ (__ \ "lineText").read[String])(ReportLine.apply _)
}

object Report {
  val reads: Reads[Report] = {
    implicit val rlR: Reads[ReportLine] = ReportLine.reads
    ((__ \ "_id").read[String]
      ~ (__ \ "inspectionResults").read[Seq[ReportLine]])(Report.apply _)
  }
}
