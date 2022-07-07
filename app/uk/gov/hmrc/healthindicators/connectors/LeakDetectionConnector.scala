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
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeakDetectionConnector @Inject() (
  httpClientV2  : HttpClientV2,
  servicesConfig: ServicesConfig
)(implicit val ec: ExecutionContext) {
  import HttpReads.Implicits._

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val leakDetectionBaseUrl: String =
    servicesConfig.baseUrl("leak-detection")

  def findLeaks(repo: String): Future[Seq[Leak]] = {
    implicit val rF = Leak.reads
    httpClientV2
      .get(url"$leakDetectionBaseUrl/api/leaks?repository=$repo")
      .execute[Seq[Leak]]
  }
}

case class Leak(
  repoName: String,
  branch  : String,
  ruleId  : String
)

object Leak {
  val reads: Reads[Leak] = {
    ( (__ \ "repoName").read[String]
    ~ (__ \ "branch"  ).read[String]
    ~ (__ \ "ruleId"  ).read[String]
    )(Leak.apply _)
  }
}
