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
import uk.gov.hmrc.healthindicators.configs.PointsConfig
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepoIndicatorServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepository: RepositoryMetricsRepository = mock[RepositoryMetricsRepository]

  private val bobbyRulesMetric: Metric =
    Metric(BobbyRuleMetricType, Seq(Result(BobbyRuleActive, "desc", None)))
  private val readMeMetricOne: Metric = Metric(ReadMeMetricType, Seq(Result(NoReadme, "desc", None)))
  private val readMeMetricTwo: Metric = Metric(ReadMeMetricType, Seq(Result(ValidReadme, "desc", None)))
  private val leakDetectionMetric: Metric =
    Metric(LeakDetectionMetricType, Seq(Result(LeakDetectionViolation, "desc", None)))
  private val repositoryMetricOne: RepositoryMetrics =
    RepositoryMetrics(
      "foo",
      Instant.now(),
      Service,
      Seq(bobbyRulesMetric, readMeMetricOne, leakDetectionMetric)
    )
  private val repositoryMetricTwo: RepositoryMetrics =
    RepositoryMetrics(
      "bar",
      Instant.now(),
      Service,
      Seq(bobbyRulesMetric, readMeMetricTwo, leakDetectionMetric)
    )

  private val pointsConfig         = new PointsConfig
  private val repoIndicatorService = new RepoIndicatorService(mockRepository, pointsConfig)

  "RepoIndicatorService" should {

    "Return a Indicator for a single Repository" in {
      when(mockRepository.latestRepositoryMetrics("foo"))
        .thenReturn(Future.successful(Some(repositoryMetricOne)))

      val result = repoIndicatorService.indicatorForRepo("foo")

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

    "Return a Indicator for each Repository in ascending order" in {
      when(mockRepository.allLatestRepositoryMetrics(None))
        .thenReturn(Future.successful(Seq(repositoryMetricOne, repositoryMetricTwo)))

      val result = repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Ascending)

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

    "Return a Indicator for each Repository in descending order, when sort equals true" in {
      when(mockRepository.allLatestRepositoryMetrics(None))
        .thenReturn(Future.successful(Seq(repositoryMetricTwo, repositoryMetricOne)))

      val result = repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Descending)

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
