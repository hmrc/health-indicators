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

package uk.gov.hmrc.healthindicators

import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.healthindicators.models.{Collector, Collectors}
import uk.gov.hmrc.healthindicators.raters.leakdetection.LeakDetectionCollector
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeCollector

class Module() extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[Collectors]).to(classOf[CollectorsProvider])
    bind(classOf[schedulers.HealthIndicatorsScheduler]).asEagerSingleton()
  }
}

@Singleton
class CollectorsProvider @Inject()(readMeCollector: ReadMeCollector, leakDetectionCollector: LeakDetectionCollector)
    extends Collectors {
  override def collect: Seq[Collector] = Seq(readMeCollector, leakDetectionCollector)
}
