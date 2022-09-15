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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.models.{AveragePlatformScore, BobbyRuleMetricType, Breakdown, DataPoint, GithubMetricType, HistoricIndicator, HistoricIndicatorAPI, Indicator, LeakDetectionMetricType, SortType, WeightedMetric}
import uk.gov.hmrc.healthindicators.persistence.{AveragePlatformScoreRepository, HistoricIndicatorsRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}


class HistoricIndicatorServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepositoryIndicator: HistoricIndicatorsRepository = mock[HistoricIndicatorsRepository]
  private val mockRepoIndicatorService: RepoIndicatorService = mock[RepoIndicatorService]
  private val mockRepositoryAverageScore: AveragePlatformScoreRepository = mock[AveragePlatformScoreRepository]

  private val historicIndicatorService = new HistoricIndicatorService(mockRepoIndicatorService, mockRepositoryIndicator, mockRepositoryAverageScore)

  private val now = Instant.now()
  private val dayBefore = Instant.now().minus(1, ChronoUnit.DAYS)

  private val historicIndicatorsFoo: Seq[HistoricIndicator] =
    Seq(HistoricIndicator("foo", now, 100), HistoricIndicator("foo", dayBefore, 50))

  private val historicIndicators: Seq[HistoricIndicator] =
    Seq(HistoricIndicator("foo", now, 100), HistoricIndicator("foo", dayBefore, 50),
      HistoricIndicator("bar", now, 75), HistoricIndicator("bar", dayBefore, 25))

  private val indicators: Seq[Indicator] =
    Seq(
      Indicator(
        "foo",
        Service,
        -200,
        Seq(
          WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
          WeightedMetric(GithubMetricType, -50, Seq(Breakdown(-50, "desc", None))),
          WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
        )
      ),
      Indicator(
        "bar",
        Service,
        -100,
        Seq(
          WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
          WeightedMetric(GithubMetricType, 50, Seq(Breakdown(50, "desc", None))),
          WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
        )
      )
    )


  "HistoricIndicatorService.collectHistoricIndicators" should {

    "Traverse all Indicators and create HistoricIndicator and calculate AveragePlatformScore, inserting them both into their own Mongo Collection" in {
      when(mockRepoIndicatorService.indicatorsForAllRepos(None, SortType.Ascending))
        .thenReturn(Future.successful(indicators))

      when(mockRepositoryIndicator.insert(any[Seq[HistoricIndicator]]))
        .thenReturn(Future.unit)

      when(mockRepositoryAverageScore.insert(any[AveragePlatformScore]))
        .thenReturn(Future.unit)

      Await.result(historicIndicatorService.collectHistoricIndicators(), 10.seconds) shouldBe ((): Unit)

      verify(mockRepositoryIndicator, times(1)).insert(any[Seq[HistoricIndicator]])

      verify(mockRepositoryAverageScore, times(1)).insert(any[AveragePlatformScore])
    }
  }

  "HistoricIndicatorService.historicIndicatorForRepo" should {

    "Return a HistoricIndicatorAPI for a specific repo" in {
      when(mockRepositoryIndicator.findAllForRepo("foo"))
        .thenReturn(Future.successful(historicIndicatorsFoo))

      val result = historicIndicatorService.historicIndicatorForRepo("foo")

      result.futureValue mustBe Some(
        HistoricIndicatorAPI("foo", Seq(DataPoint(now, 100), DataPoint(dayBefore, 50)))
      )
    }

    "Return None when repo not found" in {
      when(mockRepositoryIndicator.findAllForRepo("foo"))
        .thenReturn(Future.successful(Seq.empty))

      val result = historicIndicatorService.historicIndicatorForRepo("foo")

      result.futureValue mustBe None
    }
  }

  "HistoricIndicatorService.historicIndicatorsForAllRepos" should {

    "Return HistoricIndicatorAPI for all repos" in {
      when(mockRepositoryIndicator.findAll)
        .thenReturn(Future.successful(historicIndicators))

      val result = historicIndicatorService.historicIndicatorsForAllRepos

      result.futureValue.toSet mustBe Set(
        HistoricIndicatorAPI("foo", Seq(DataPoint(now, 100), DataPoint(dayBefore, 50))),
        HistoricIndicatorAPI("bar", Seq(DataPoint(now, 75), DataPoint(dayBefore, 25))))
    }
  }

}
