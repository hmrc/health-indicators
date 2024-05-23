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

package uk.gov.hmrc.healthindicators.services

import java.time.Instant
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.PointsConfig
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepoIndicatorServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepository: RepositoryMetricsRepository = mock[RepositoryMetricsRepository]

  private val GithubMetricOne: Metric = Metric(GithubMetricType, Seq(Result(NoReadme, "desc", None)))
  private val GithubMetricTwo: Metric = Metric(GithubMetricType, Seq(Result(CleanGithub, "desc", None)))
  private val leakDetectionMetric: Metric =
    Metric(LeakDetectionMetricType, Seq(Result(LeakDetectionViolation, "desc", None)))
  private val repositoryMetricOne: RepositoryMetrics =
    RepositoryMetrics(
      "foo",
      Instant.now(),
      Service,
      Seq(GithubMetricOne, leakDetectionMetric)
    )
  private val repositoryMetricTwo: RepositoryMetrics =
    RepositoryMetrics(
      "bar",
      Instant.now(),
      Service,
      Seq(GithubMetricTwo, leakDetectionMetric)
    )

  private val pointsConfig         = new PointsConfig
  private val repoIndicatorService = new RepoIndicatorService(mockRepository, pointsConfig)

  private val noReadMe                = pointsConfig.points(NoReadme)
  private val cleanGithub             = pointsConfig.points(CleanGithub)
  private val leakDetectionViolation  = pointsConfig.points(LeakDetectionViolation)

  "RepoIndicatorService" should {
    "Return a Indicator for a single Repository" in {
      when(mockRepository.getRepositoryMetrics("foo"))
        .thenReturn(Future.successful(Some(repositoryMetricOne)))

      val result = repoIndicatorService.indicatorForRepo("foo")

      result.futureValue shouldBe Some(
        Indicator(
          "foo",
          Service,
          noReadMe + leakDetectionViolation,
          Seq(
            WeightedMetric(GithubMetricType, noReadMe, Seq(Breakdown(noReadMe, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, leakDetectionViolation, Seq(Breakdown(leakDetectionViolation, "desc", None)))
          )
        )
      )
    }

    "Return a Indicator for each Repository in ascending order" in {
      when(mockRepository.findAll(None))
        .thenReturn(Future.successful(Seq(repositoryMetricOne, repositoryMetricTwo)))

      val result = repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Ascending)

      result.futureValue shouldBe Seq(
        Indicator(
          "foo",
          Service,
          noReadMe + leakDetectionViolation,
          Seq(
            WeightedMetric(GithubMetricType, noReadMe, Seq(Breakdown(noReadMe, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, leakDetectionViolation, Seq(Breakdown(leakDetectionViolation, "desc", None)))
          )
        ),
        Indicator(
          "bar",
          Service,
          cleanGithub + leakDetectionViolation,
          Seq(
            WeightedMetric(GithubMetricType, cleanGithub, Seq(Breakdown(cleanGithub, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, leakDetectionViolation, Seq(Breakdown(leakDetectionViolation, "desc", None)))
          )
        )
      )
    }

    "Return a Indicator for each Repository in descending order, when sort equals true" in {
      when(mockRepository.findAll(None))
        .thenReturn(Future.successful(Seq(repositoryMetricTwo, repositoryMetricOne)))

      val result = repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Descending)

      result.futureValue shouldBe Seq(
        Indicator(
          "bar",
          Service,
          cleanGithub + leakDetectionViolation,
          Seq(
            WeightedMetric(GithubMetricType, cleanGithub, Seq(Breakdown(cleanGithub, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, leakDetectionViolation, Seq(Breakdown(leakDetectionViolation, "desc", None)))
          )
        ),
        Indicator(
          "foo",
          Service,
          noReadMe + leakDetectionViolation,
          Seq(
            WeightedMetric(GithubMetricType, noReadMe, Seq(Breakdown(noReadMe, "desc", None))),
            WeightedMetric(LeakDetectionMetricType, leakDetectionViolation, Seq(Breakdown(leakDetectionViolation, "desc", None)))
          )
        )
      )
    }
  }
}
