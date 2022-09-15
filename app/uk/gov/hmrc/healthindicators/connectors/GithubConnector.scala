/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Reads, __}
import uk.gov.hmrc.healthindicators.configs.GithubConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GithubConnector @Inject() (
  httpClientV2: HttpClientV2,
  githubConfig: GithubConfig
)(implicit ec: ExecutionContext) {
  import HttpReads.Implicits._

  private val configKey = githubConfig.token

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  def findReadMe(repo: String): Future[Option[String]] =
    httpClientV2
      .get(url"${githubConfig.rawUrl}/hmrc/$repo/HEAD/README.md")
      .setHeader("Authorization" -> s"token $configKey")
      .withProxy
      .execute[Option[HttpResponse]]
      .map(_.map(_.body))

  def getOpenPRs(repo: String): Future[Option[Seq[OpenPR]]] = {
    val url =
      url"${githubConfig.restUrl}/repos/hmrc/$repo/pulls?state=open"
    implicit val oR: Reads[OpenPR] = OpenPR.reads
    httpClientV2
      .get(url)
      .setHeader("Authorization" -> s"token $configKey")
      .withProxy
      .execute[Option[Seq[OpenPR]]]
  }
}

case class OpenPR(title: String, createdAt: LocalDate, updatedAt: LocalDate)

object OpenPR {
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

  implicit val readsDate: Reads[LocalDate] = (json: JsValue) =>
    json.validate[String].map(LocalDate.parse(_, dateFormatter))

  val reads: Reads[OpenPR] =
    ( (__ \ "title"     ).read[String]
    ~ (__ \ "created_at").read[LocalDate]
    ~ (__ \ "updated_at").read[LocalDate]
    )(OpenPR.apply _)
}
