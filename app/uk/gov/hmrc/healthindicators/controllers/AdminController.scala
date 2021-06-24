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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.healthindicators.services.MetricCollectionService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdminController @Inject() (metricProductionService: MetricCollectionService, cc: ControllerComponents)(implicit
  ec: ExecutionContext
) extends BackendController(cc) {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  def rerun(): Action[AnyContent] =
    Action.async {
      metricProductionService.collectAll().recover {
        case e: Throwable => e.printStackTrace()
      }
      Future.successful(Accepted)
    }
}
