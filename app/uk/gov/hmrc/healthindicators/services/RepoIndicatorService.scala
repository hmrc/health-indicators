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

import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.connectors.RepoType
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.MetricsPersistence

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepoIndicatorService @Inject()(repository: MetricsPersistence, scoreConfig: ScoreConfig)(implicit
                                                                                               val ec: ExecutionContext
) {

  def indicatorForRepo(repo: String): Future[Option[Indicator]] =
    repository.latestRepositoryMetrics(repo).map { maybeHealthIndicator =>
      indicate(maybeHealthIndicator.toSeq).headOption
    }

  def indicatorsForAllRepos(repoType: Option[RepoType], sort: SortType): Future[Seq[Indicator]] =
    repository.allLatestRepositoryMetrics(repoType).map { healthIndicators =>
      val repoRatings = indicate(healthIndicators).sortBy(_.overallScore)
      sort match {
        case SortType.Ascending  => repoRatings
        case SortType.Descending => repoRatings.reverse
      }
    }

  def indicate(healthIndicators: Seq[RepositoryMetrics]): Seq[Indicator] =
    for {
      indicator <- healthIndicators
      rating           = indicator.metrics.map(applyWeighting)
      repositoryScore  = rating.map(_.score).sum
      repositoryRating = Indicator(indicator.repoName, indicator.repoType, repositoryScore, rating)
    } yield repositoryRating

  private def applyWeighting(metric: Metric): WeightedMetric = {
    val scores: Seq[Breakdown] = metric.results.map(createScore)
    val overallScore = scores.map(_.points).sum
    WeightedMetric(metric.metricType, overallScore, scores)
  }

  private def createScore(result: Result): Breakdown = {
    val points = scoreConfig.scores(result.resultType)
    Breakdown(points, result.description, result.href)
  }

}
