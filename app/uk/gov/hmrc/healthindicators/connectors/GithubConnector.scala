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

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Reads, __}
import uk.gov.hmrc.healthindicators.configs.GithubConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, NotFoundException}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GithubConnector @Inject() (
  httpClient: HttpClient,
  githubConfig: GithubConfig
)(implicit ec: ExecutionContext) {

  private val configKey = githubConfig.token

  def findReadMe(repo: String): Future[Option[String]] = {
    val url =
      s"${githubConfig.rawUrl}/hmrc/$repo/master/README.md"
    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(("Authorization", s"token $configKey"))

    httpClient.GET[HttpResponse](url).map(r => Some(r.body))
  }.recoverWith {
    case _: NotFoundException => Future.successful(None)
  }

  def getOpenPRs(repo: String): Future[Option[Seq[OpenPR]]] = {
    val url =
      s"${githubConfig.restUrl}/repos/hmrc/$repo/pulls"
    implicit val hc: HeaderCarrier = HeaderCarrier().withExtraHeaders(("Authorization", s"token $configKey"))
    implicit val oR: Reads[OpenPR] = OpenPR.reads
    val logger = Logger(this.getClass)

    httpClient.GET[Option[Seq[OpenPR]]](url).recoverWith {
      case _: NotFoundException  =>
        logger.error(s"An error occurred when connecting to ${githubConfig.restUrl}repos/hmrc/$repo/pulls")
        Future.successful(None)
    }
  }
}

case class OpenPR(title: String, createdAt: LocalDate, updatedAt: LocalDate)

object OpenPR {
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  implicit val readsDate: Reads[LocalDate] = (json: JsValue) =>
    json.validate[String].map(LocalDate.parse(_, dateFormatter))

  val reads: Reads[OpenPR] =
    ((__ \ "title").read[String]
      ~ (__ \ "created_at").read[LocalDate]
      ~(__ \ "updated_at").read[LocalDate])(OpenPR.apply _)
}