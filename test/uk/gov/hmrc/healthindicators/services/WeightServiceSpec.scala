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

package uk.gov.hmrc.healthindicators.services

import java.time.LocalDateTime

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.WeightsConfig
import uk.gov.hmrc.healthindicators.models.HealthIndicators
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.healthindicators.raters.leakdetection.LeakDetectionRating
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeRating

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class WeightServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockHealthIndicatorsRepository = mock[HealthIndicatorsRepository]
  val mockWeightsConfig = mock[WeightsConfig]

  val weightService = new WeightService(mockHealthIndicatorsRepository, mockWeightsConfig)

  "weightedScore" should {

    "Return None when no indicators are found in the collection" in {
      when(mockHealthIndicatorsRepository.latestIndicators("foo")) thenReturn Future.successful(None)

      val result = weightService.weightedScore("foo")

      Await.result(result, 5 seconds) mustBe None
    }

    "Return a Weighted Score based on Ratings and Weights" in {
      when(mockHealthIndicatorsRepository.latestIndicators("foo")) thenReturn Future.successful(Some(TestData.healthIndicator100))
      when(mockHealthIndicatorsRepository.latestIndicators("bar")) thenReturn Future.successful(Some(TestData.healthIndicator67))

      when(mockWeightsConfig.weightsLookup) thenReturn Map("ReadMeRating" -> 2.0, "LeakDetectionRating" -> 1.0)

      val result100 = weightService.weightedScore("foo")
      val result67 = weightService.weightedScore("bar")

      Await.result(result100, 5 seconds) mustBe Some(100)
      Await.result(result67, 5 seconds) mustBe Some(67)
    }
  }
}

object TestData {
  val healthIndicator100 = HealthIndicators("foo", LocalDateTime.now(), Seq(ReadMeRating(5000, "Valid README found"), LeakDetectionRating(0)))
  val healthIndicator67 = HealthIndicators("bar", LocalDateTime.now(), Seq(ReadMeRating(3000, "Valid README found"), LeakDetectionRating(4)))
}
