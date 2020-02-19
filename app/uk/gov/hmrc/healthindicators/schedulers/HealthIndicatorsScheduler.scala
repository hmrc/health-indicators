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

package uk.gov.hmrc.healthindicators.schedulers

import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.persistence.MongoLocks
import uk.gov.hmrc.healthindicators.services.HealthIndicatorsService
import uk.gov.hmrc.healthindicators.utils.SchedulerUtils
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HealthIndicatorsScheduler @Inject()(
  healthIndicatorsService: HealthIndicatorsService,
  config: SchedulerConfigs,
  mongoLocks: MongoLocks
)(implicit actorSystem: ActorSystem, applicationLifecycle: ApplicationLifecycle)
    extends SchedulerUtils {

  private implicit val hc = HeaderCarrier()

  scheduleWithLock("Health Indicators Reloader", config.healthIndicatorsScheduler, mongoLocks.healthIndicatorsMongoLock) {

    for {
      _ <- healthIndicatorsService.insertRatings.recover {
            case e: Throwable => Logger.error("Error inserting Health Indicators", e)
          }
      _ = Logger.info("Finished inserting Health Indicators")
    } yield ()

  }
}
