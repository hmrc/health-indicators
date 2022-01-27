/*
 * Copyright 2022 HM Revenue & Customs
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

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.AveragePlatformScore
import uk.gov.hmrc.healthindicators.persistence.AveragePlatformScoreRepository

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class AveragePlatformScoreServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepositoryAverageScore: AveragePlatformScoreRepository = mock[AveragePlatformScoreRepository]
  private val averagePlatformScoreService = new AveragePlatformScoreService(mockRepositoryAverageScore)

  private val now = Instant.now()
  private val dayBefore = Instant.now().minus(1, ChronoUnit.DAYS)

  private val averagePlatformScoreNow: AveragePlatformScore = AveragePlatformScore(now, 100)
  private val averagePlatformScoreDayBefore: AveragePlatformScore = AveragePlatformScore(dayBefore, 50)

  private val averageScores: Seq[AveragePlatformScore] = Seq(averagePlatformScoreNow, averagePlatformScoreDayBefore)

  "AveragePlatformScoreService.latest" should {

    "Return latest AveragePlatformScore by most recent timestamp" in {
      when(mockRepositoryAverageScore.findLatest).thenReturn(Future.successful(Some(averagePlatformScoreNow)))

      val result = averagePlatformScoreService.latest()

      result.futureValue shouldBe Some(averagePlatformScoreNow)
    }
  }

  "AveragePlatformScoreService.historic" should {

    "Return Historic Averages in descending order" in {
      when(mockRepositoryAverageScore.findAll()).thenReturn(Future.successful(averageScores))

      val result = averagePlatformScoreService.historic()

      result.futureValue shouldBe averageScores
    }
  }




}
