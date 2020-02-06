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

package uk.gov.hmrc.healthindicators.connectors

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.healthindicators.configs.HealthIndicatorsConfig
import uk.gov.hmrc.healthindicators.models.Repository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject()(
  httpClient: HttpClient,
  healthIndicatorsConfig: HealthIndicatorsConfig
)(implicit val ec: ExecutionContext) {

  private val teamsAndRepositoriesBaseUrl: String = healthIndicatorsConfig.teamsAndRepositoriesUrl

  def allRepositories(implicit hc: HeaderCarrier): Future[List[Repository]] = {
    implicit val rF = Repository.reads
    httpClient.GET[List[Repository]](teamsAndRepositoriesBaseUrl + s"/api/repositories")
  }
}
