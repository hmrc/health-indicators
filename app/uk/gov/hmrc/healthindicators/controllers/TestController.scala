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
import play.api.libs.json.Writes
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.healthindicators.connectors.LeakDetectionConnector
import uk.gov.hmrc.healthindicators.models.RepositoryRating
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.healthindicators.services.HealthIndicatorService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

@Singleton
class TestController @Inject() (
  leakDetectionConnector: LeakDetectionConnector,
  repoRatingsPersistence: HealthIndicatorsRepository,
  repository: HealthIndicatorsRepository,
  ratingService: HealthIndicatorService,
  cc: ControllerComponents
) extends BackendController(cc) {

  implicit val rrW: Writes[RepositoryRating] = RepositoryRating.writes

  def test: Action[AnyContent] =
    Action {
      val result = repository.latestAllRepositoryHealthIndicators()
      Ok(Await.result(result, 5.seconds).toString)
    }
}
