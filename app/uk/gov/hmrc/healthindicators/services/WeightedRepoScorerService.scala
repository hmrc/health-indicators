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

import cats.data.OptionT
import cats.implicits._
import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Rating, RepoRatings, RepoScoreBreakdown}
import uk.gov.hmrc.healthindicators.persistence.RepoRatingsPersistence


import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}


class WeightedRepoScorerService @Inject()(repository: RepoRatingsPersistence,
                                          weightService: WeightService,
                                         )(implicit val ec: ExecutionContext) {





  def repoScore(repo: String): Future[Option[RepoScoreBreakdown]] = {


    repository.latestRatingsForRepo(repo).map(_.map(repoRating => {
      RepoScoreBreakdown(repo, weightService.weightedScore(repoRating), repoRating.ratings)
    }))

//    for {
//      repoRating <- repository.latestRatingsForRepo(repo)
//      ratings: Map[Nothing, Nothing] = repoRating.map(_.ratings)
//      weightedRepoScore = repoRating.map(weightService.weightedScore)
//      ratingBreakdown =
//    }ratingBreakdown






//    val repoRating: Future[Option[RepoRatings]] = repository.latestRatingsForRepo(repo)
//
//    val ratings: Future[Option[Seq[Rating]]] = repoRating.map(_.map(_.ratings))
//
//    val repoWeightedScore: Future[Option[Int]] = OptionT(repository.latestRatingsForRepo(repo))
//      .map(weightService.weightedScore)
//      .value
//
//    val ratingBreakdown: Future[Option[Map[String, Int]]] = ratings.map(_.map(_.map(h => (h.ratingType.toString, h.rating)).toMap))
//
//    ratingBreakdown


  }



  def repoScoreAllRepos(): Future[Map[String, Int]] =
    for {
      indicators <- repository.latestRatings()
      scores = indicators.map(h => (h.repo, weightService.weightedScore(h))).toMap
    } yield scores


}
