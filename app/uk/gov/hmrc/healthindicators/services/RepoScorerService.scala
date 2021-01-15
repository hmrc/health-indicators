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

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.models.{RepositoryRating, Score}
import uk.gov.hmrc.healthindicators.persistence.RepoRatingsPersistence

import scala.concurrent.{ExecutionContext, Future}

class RepoScorerService @Inject() (repository: RepoRatingsPersistence, scoreConfig: ScoreConfig)(implicit
  val ec: ExecutionContext
) {

  //RepositoryRating(repositoryName: String, repositoryScore: Int, ratings: Seq[Rating])

  def repoScore(repo: String): Future[Option[RepositoryRating]] =
    repository
      .latestRatingsForRepo(repo)
      .map(_.map { serviceHealthIndicator =>
        serviceHealthIndicator.indicators.groupBy(_.result.getClass)
        val scores = serviceHealthIndicator.indicators.map { indicator =>
          (indicator.result -> Score(scoreConfig.scores(indicator.result.value()), indicator.description, indicator.href)
        }
      })

  def repoScoreAllRepos(): Future[Map[String, Int]] =
    for {
      ratings <- repository.latestRatings()
      scores = ratings.map(h => (h.repo, sumScores(h))).toMap
    } yield scores

  def sumScores(repoRatings: RepoRatings): Int =
    repoRatings.ratings.map(r => r.calculateScore(scoreConfig)).sum

}
