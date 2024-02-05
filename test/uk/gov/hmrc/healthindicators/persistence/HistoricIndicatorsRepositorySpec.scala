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

package uk.gov.hmrc.healthindicators.persistence

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.HistoricIndicator
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class HistoricIndicatorsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[HistoricIndicator]{

  override protected val repository = new HistoricIndicatorsRepository(mongoComponent)

  "HistoricIndicatorRepository" should {
    val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)
    val historicIndicatorOneA  = HistoricIndicator("test1", now, 100)
    val historicIndicatorTwo   = HistoricIndicator("test2", now, 50)
    val historicIndicatorThree = HistoricIndicator("test3", now, 75)
    val historicIndicatorOneB  = HistoricIndicator("test1", now, 25)

    "insert and find all correctly" in {
      repository.insert(historicIndicatorOneA).futureValue
      repository.findAll.futureValue should contain(historicIndicatorOneA)
    }

    "insert and find all for specific repo correctly" in {
      repository.insert(historicIndicatorOneA ).futureValue
      repository.insert(historicIndicatorTwo  ).futureValue
      repository.insert(historicIndicatorThree).futureValue
      repository.insert(historicIndicatorOneB ).futureValue
      repository.findAllForRepo("test1").futureValue should contain(historicIndicatorOneA)
      repository.findAllForRepo("test1").futureValue should contain(historicIndicatorOneB)
      repository.findAllForRepo("test1").futureValue shouldNot contain(historicIndicatorTwo)
    }
  }
}
