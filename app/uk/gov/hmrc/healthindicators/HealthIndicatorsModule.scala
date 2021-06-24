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

package uk.gov.hmrc.healthindicators

import com.google.inject.{AbstractModule, Provides}
import play.api.Logger
import uk.gov.hmrc.healthindicators.metrics._
import uk.gov.hmrc.healthindicators.metricproducers.{AlertConfigMetricProducer, BobbyRulesMetricProducer, BuildStabilityMetricProducer, LeakDetectionMetricProducer, MetricProducer, ReadMeMetricProducer, StalePrMetricProducer}

class HealthIndicatorsModule() extends AbstractModule {

  private val logger = Logger(this.getClass)

  override def configure(): Unit =
    bind(classOf[schedulers.MetricScheduler]).asEagerSingleton()

  @Provides
  def producers(
    bobbyRulesMetricProducer: BobbyRulesMetricProducer,
    leakDetectionMetricProducer: LeakDetectionMetricProducer,
    readMeMetricProducer: ReadMeMetricProducer,
    buildStabilityMetricProducer: BuildStabilityMetricProducer,
    stalePrMetricProducer: StalePrMetricProducer,
    alertConfigMetricProducer: AlertConfigMetricProducer
  ): List[MetricProducer] = {
    val producers =
      List(
        bobbyRulesMetricProducer,
        leakDetectionMetricProducer,
        readMeMetricProducer,
        buildStabilityMetricProducer,
        alertConfigMetricProducer,
        stalePrMetricProducer
      )
    logger.info(s"Loaded Metric Producers: ${producers.map(_.getClass.getSimpleName).mkString("[\n", "\n", "\n]")}")
    producers
  }
}
