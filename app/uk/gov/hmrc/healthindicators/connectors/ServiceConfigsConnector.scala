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

import uk.gov.hmrc.healthindicators.configs.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import play.api.libs.json._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReads.Implicits._

class ServiceConfigsConnector @Inject() (httpClient: HttpClient, ratersConfig: AppConfig)(implicit
  val ec: ExecutionContext
) {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val serviceConfigsBaseURL: String = ratersConfig.serviceConfigs

  def findAlertConfigs(repo: String): Future[Option[AlertConfig]] = {
    implicit val aR: Reads[AlertConfig] = AlertConfig.reads
    httpClient.GET[Option[AlertConfig]](s"$serviceConfigsBaseURL/alert-configs/$repo")
  }

}

case class AlertConfig(
  production: Boolean
)

object AlertConfig {
  implicit val reads: Reads[AlertConfig] = Json.reads[AlertConfig]
}
