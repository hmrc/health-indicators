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

package uk.gov.hmrc.healthindicators.schedulers

import akka.actor.ActorSystem
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.persistence.MongoLock
import uk.gov.hmrc.healthindicators.services.{HistoricIndicatorService, MetricCollectionService}
import uk.gov.hmrc.healthindicators.utils.SchedulerUtils
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class MetricScheduler @Inject() (
  metricCollectionService: MetricCollectionService,
  historicIndicatorService: HistoricIndicatorService,
  config: SchedulerConfigs,
  mongoLocks: MongoLock
)(implicit actorSystem: ActorSystem, applicationLifecycle: ApplicationLifecycle)
    extends SchedulerUtils {

  private val logger                     = Logger(this.getClass)
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  scheduleWithLock("Metric Reloader", config.metricScheduler, mongoLocks.metricsMongoLock) {
    for {
    metric  <- metricCollectionService.collectAll()
      .recover {
        case e: Throwable => logger.error("Error inserting Metrics", e)
      }

    historic <- historicIndicatorService.collectHistoricIndicators()
      .recover {
        case e: Throwable => logger.error("Error inserting Historic Indicators", e)
      }

    _ = logger.info("Finished inserting")

    } yield ()
  }
}
