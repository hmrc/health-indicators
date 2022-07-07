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

import play.api.libs.json._
import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class ServiceConfigsConnector @Inject()(
  httpClientV2  : HttpClientV2,
  servicesConfig: ServicesConfig
)(implicit
  ec: ExecutionContext
) {
  import HttpReads.Implicits._

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val serviceConfigsBaseURL: String =
    servicesConfig.baseUrl("service-configs")

  def findAlertConfigs(repo: String): Future[Option[AlertConfig]] = {
    implicit val aR: Reads[AlertConfig] = AlertConfig.reads
    httpClientV2
      .get(url"$serviceConfigsBaseURL/alert-configs/$repo")
      .execute[Option[AlertConfig]]
  }
}

case class AlertConfig(
  production: Boolean
)

object AlertConfig {
  implicit val reads: Reads[AlertConfig] = Json.reads[AlertConfig]
}
