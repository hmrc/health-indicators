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

package uk.gov.hmrc.healthindicators.services

import java.time.Instant
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.MetricsPersistence

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepoIndicatorServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepository: MetricsPersistence = mock[MetricsPersistence]

  private val bobbyRulesRating: Metric =
    Metric(BobbyRuleMetricType, Seq(Result(BobbyRuleActive, "desc", None)))
  private val readMeRatingOne: Metric = Metric(ReadMeMetricType, Seq(Result(NoReadme, "desc", None)))
  private val readMeRatingTwo: Metric = Metric(ReadMeMetricType, Seq(Result(ValidReadme, "desc", None)))
  private val leakDetectionRating: Metric =
    Metric(LeakDetectionMetricType, Seq(Result(LeakDetectionViolation, "desc", None)))
  private val healthIndicatorOne: RepositoryMetrics =
    RepositoryMetrics(
      "foo",
      Instant.now(),
      Service,
      Seq(bobbyRulesRating, readMeRatingOne, leakDetectionRating)
    )
  private val healthIndicatorTwo: RepositoryMetrics =
    RepositoryMetrics(
      "bar",
      Instant.now(),
      Service,
      Seq(bobbyRulesRating, readMeRatingTwo, leakDetectionRating)
    )

  private val scoreConfig       = new ScoreConfig
  private val repoScorerService = new RepoIndicatorService(mockRepository, scoreConfig)

  "repoScore" should {

    "Return a Rating for a single Repository" in {
      when(mockRepository.latestRepositoryMetrics("foo"))
        .thenReturn(Future.successful(Some(healthIndicatorOne)))

      val result = repoScorerService.indicatorForRepo("foo")

      result.futureValue mustBe Some(
        Indicator(
          "foo",
          Service,
          -200,
          Seq(
            WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
            WeightedMetric(ReadMeMetricType, -50, Seq(Breakdown(-50, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
          )
        )
      )
    }

    "Return a Rating for each Repository in ascending order" in {
      when(mockRepository.allLatestRepositoryMetrics(None))
        .thenReturn(Future.successful(Seq(healthIndicatorOne, healthIndicatorTwo)))

      val result = repoScorerService.indicatorsForAllRepos(repoType = None, SortType.Ascending)

      result.futureValue mustBe Seq(
        Indicator(
          "foo",
          Service,
          -200,
          Seq(
            WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
            WeightedMetric(ReadMeMetricType, -50, Seq(Breakdown(-50, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
          )
        ),
        Indicator(
          "bar",
          Service,
          -100,
          Seq(
            WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
            WeightedMetric(ReadMeMetricType, 50, Seq(Breakdown(50, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
          )
        )
      )
    }

    "Return a Rating for each Repository in descending order, when sort equals true" in {
      when(mockRepository.allLatestRepositoryMetrics(None))
        .thenReturn(Future.successful(Seq(healthIndicatorTwo, healthIndicatorOne)))

      val result = repoScorerService.indicatorsForAllRepos(repoType = None, SortType.Descending)

      result.futureValue mustBe Seq(
        Indicator(
          "bar",
          Service,
          -100,
          Seq(
            WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
            WeightedMetric(ReadMeMetricType, 50, Seq(Breakdown(50, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
          )
        ),
        Indicator(
          "foo",
          Service,
          -200,
          Seq(
            WeightedMetric(BobbyRuleMetricType, -100, Seq(Breakdown(-100, "desc", None))),
            WeightedMetric(ReadMeMetricType, -50, Seq(Breakdown(-50, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, -50, Seq(Breakdown(-50, "desc", None)))
          )
        )
      )
    }
  }
}
