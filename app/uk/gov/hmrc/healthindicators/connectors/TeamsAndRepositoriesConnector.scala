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
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.healthindicators.configs.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject() (
  httpClient: HttpClient,
  healthIndicatorsConfig: AppConfig
)(implicit val ec: ExecutionContext) {

  private val teamsAndRepositoriesBaseUrl: String = healthIndicatorsConfig.teamsAndRepositoriesUrl

  def allRepositories(implicit hc: HeaderCarrier): Future[List[TeamsAndRepos]] = {
    implicit val reads: Reads[TeamsAndRepos] = TeamsAndRepos.reads
    httpClient.GET[List[TeamsAndRepos]](s"$teamsAndRepositoriesBaseUrl/api/repositories")
  }
}

case class TeamsAndRepos(
  name: String
)

object TeamsAndRepos {
  val reads: Reads[TeamsAndRepos] =
    (__ \ "name").read[String].map(TeamsAndRepos.apply)
}
