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

package uk.gov.hmrc.healthindicators.models

import org.mockito.MockitoSugar
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import java.time.temporal.ChronoUnit

class HistoricIndicatorAPITest extends AnyWordSpec with Matchers with MockitoSugar with Eventually {

  private val now = Instant.now()

  private val singleHistoricIndicator = Seq(HistoricIndicator("test", now, 100))

  private val multipleHistoricIndicators = Seq(HistoricIndicator("test", now, 100),
    HistoricIndicator("test", now.plus(1, ChronoUnit.DAYS), 200),
    HistoricIndicator("test", now.plus(2, ChronoUnit.DAYS), 150))

  private val historicIndicatorAPISingleDataPoint = HistoricIndicatorAPI("test", Seq(DataPoint(now, 100)))

  private val historicIndicatorAPIMultipleDataPoints = HistoricIndicatorAPI("test", Seq(DataPoint(now, 100),
    DataPoint(now.plus(1, ChronoUnit.DAYS), 200),
    DataPoint(now.plus(2, ChronoUnit.DAYS), 150)))



  "HistoricIndicatorAPI.fromHistoricIndicators" should {

    "return None when Seq is Empty" in {
      HistoricIndicatorAPI.fromHistoricIndicators(Seq.empty) shouldBe None
    }

    "return HistoricIndicatorAPI with a single Data Point" in {
      HistoricIndicatorAPI.fromHistoricIndicators(singleHistoricIndicator) shouldBe Some(historicIndicatorAPISingleDataPoint)
    }

    "return HistoricIndicatorAPI with multiple Data Points" in {
      HistoricIndicatorAPI.fromHistoricIndicators(multipleHistoricIndicators) shouldBe Some(historicIndicatorAPIMultipleDataPoints)
    }
  }
}


