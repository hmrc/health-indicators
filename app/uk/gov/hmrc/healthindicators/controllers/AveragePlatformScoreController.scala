/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.healthindicators.models.AveragePlatformScore
import uk.gov.hmrc.healthindicators.services.AveragePlatformScoreService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AveragePlatformScoreController @Inject() (
  averagePlatformScoreService: AveragePlatformScoreService,
  cc                         : ControllerComponents
)(implicit
  ec: ExecutionContext
) extends BackendController(cc) {

  val history: Action[AnyContent] =
    Action.async {
      implicit val apf: Format[AveragePlatformScore] = AveragePlatformScore.format
      for {
        averages: Seq[AveragePlatformScore] <- averagePlatformScoreService.historic()
      } yield Ok(Json.toJson(averages))
    }

  val latest: Action[AnyContent] =
    Action.async {
      implicit val apf: Format[AveragePlatformScore] = AveragePlatformScore.format
      for {
        average: Option[AveragePlatformScore] <- averagePlatformScoreService.latest()
      } yield Ok(Json.toJson(average))
    }
}
