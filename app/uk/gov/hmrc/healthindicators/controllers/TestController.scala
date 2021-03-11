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

package uk.gov.hmrc.healthindicators.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.healthindicators.connectors.{JenkinsConnector, JenkinsUrl, LeakDetectionConnector, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.models.RepositoryRating
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.healthindicators.services.HealthIndicatorService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.DurationInt

@Singleton
class TestController @Inject() (
  leakDetectionConnector: LeakDetectionConnector,
  repoRatingsPersistence: HealthIndicatorsRepository,
  repository: HealthIndicatorsRepository,
  ratingService: HealthIndicatorService,
  teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
  jenkinsConnector: JenkinsConnector,
  cc: ControllerComponents
) (implicit ec: ExecutionContext)
  extends BackendController(cc) {

  def test(repo: String): Action[AnyContent] = {
    Action.async {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      for {
        url <- teamsAndRepositoriesConnector.getJenkinsUrl(repo)
        result = Ok(Json.toJson(url))
      } yield result
    }
  }
}
