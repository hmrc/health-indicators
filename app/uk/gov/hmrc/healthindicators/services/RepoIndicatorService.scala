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

import uk.gov.hmrc.healthindicators.configs.PointsConfig
import uk.gov.hmrc.healthindicators.connectors.RepoType
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepoIndicatorService @Inject() (repository: RepositoryMetricsRepository, pointsConfig: PointsConfig)(implicit
  val ec: ExecutionContext
) {

  def indicatorForRepo(repo: String): Future[Option[Indicator]] =
    repository.getRepositoryMetrics(repo).map { maybeRepositoryMetric =>
      indicate(maybeRepositoryMetric.toSeq).headOption
    }

  def indicatorsForAllRepos(repoType: Option[RepoType], sort: SortType): Future[Seq[Indicator]] =
    repository.getAllRepositoryMetrics(repoType).map { repositoryMetrics =>
      val sortingBy: Indicator => Int = sort match {
        case SortType.Ascending  => _.overallScore
        case SortType.Descending => -_.overallScore
      }
      indicate(repositoryMetrics).sortBy(sortingBy)
    }

  def indicate(repositoryMetrics: Seq[RepositoryMetrics]): Seq[Indicator] =
    for {
      repositoryMetrics <- repositoryMetrics
      weightedMetric = repositoryMetrics.metrics.map(applyWeighting)
      overallScore   = weightedMetric.map(_.score).sum
      indicator      = Indicator(repositoryMetrics.repoName, repositoryMetrics.repoType, overallScore, weightedMetric)
    } yield indicator

  private def applyWeighting(metric: Metric): WeightedMetric = {
    val scores: Seq[Breakdown] = metric.results.map(createScore)
    val overallScore           = scores.map(_.points).sum
    WeightedMetric(metric.metricType, overallScore, scores)
  }

  private def createScore(result: Result): Breakdown = {
    val points = pointsConfig.points(result.resultType)
    Breakdown(points, result.description, result.href)
  }

}
