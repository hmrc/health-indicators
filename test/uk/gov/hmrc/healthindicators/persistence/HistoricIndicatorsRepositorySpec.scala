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

package uk.gov.hmrc.healthindicators.persistence

import java.time.Instant
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.models.HistoricIndicator
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class HistoricIndicatorsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[HistoricIndicator]{

  private val config: Configuration = Configuration(
    "metrics.refresh.enabled"      -> "false",
    "metrics.refresh.interval"     -> "5.minutes",
    "metrics.refresh.initialDelay" -> "5.minutes"
  )

  private val schedulerConfigs = new SchedulerConfigs(config)

  override protected val repository = new HistoricIndicatorsRepository(mongoComponent, schedulerConfigs)

  "HistoricIndicatorRepository" should {

    val historicIndicatorOneA = HistoricIndicator("test1", Instant.now, 100)
    val historicIndicatorTwo = HistoricIndicator("test2", Instant.now, 50)
    val historicIndicatorThree = HistoricIndicator("test3", Instant.now, 75)

    val historicIndicatorOneB = HistoricIndicator("test1", Instant.now, 25)


    "insert and find all correctly" in {
      repository.insert(historicIndicatorOneA)
      repository.findAll.futureValue must contain(historicIndicatorOneA)
    }

    "insert and find all for specific repo correctly" in {
      repository.insert(historicIndicatorOneA)
      repository.insert(historicIndicatorTwo)
      repository.insert(historicIndicatorThree)
      repository.insert(historicIndicatorOneB)
      repository.findAllForRepo("test1").futureValue must contain(historicIndicatorOneA)
      repository.findAllForRepo("test1").futureValue must contain(historicIndicatorOneB)
      repository.findAllForRepo("test1").futureValue mustNot contain(historicIndicatorTwo)
    }
  }

}
