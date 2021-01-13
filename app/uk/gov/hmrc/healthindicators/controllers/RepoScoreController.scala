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
import uk.gov.hmrc.healthindicators.models.RepoScoreBreakdown
import uk.gov.hmrc.healthindicators.services.RepoScorerService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

@Singleton
class RepoScoreController @Inject() (
  repoScorerService: RepoScorerService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  implicit val rsbw: Writes[RepoScoreBreakdown] = RepoScoreBreakdown.apiWrites

  def scoreForRepo(repo: String): Action[AnyContent] =
    Action.async { implicit request =>
      for {
        score <- repoScorerService.repoScore(repo)
        result = score.map(s => Ok(Json.toJson(s))).getOrElse(NotFound)
      } yield result
    }

  def scoreAllRepos(): Action[AnyContent] =
    Action.async { implicit request =>
      for {
        mapScores <- repoScorerService.repoScoreAllRepos()
        result = Ok(Json.toJson(mapScores))
      } yield result
    }
}
