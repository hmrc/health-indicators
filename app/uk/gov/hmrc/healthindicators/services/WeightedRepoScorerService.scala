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
import uk.gov.hmrc.healthindicators.persistence.RepoRatingsPersistence


import scala.concurrent.{ExecutionContext, Future}

class WeightedRepoScorerService @Inject()(repository: RepoRatingsPersistence,
                                          weightService: WeightService)(implicit val ec: ExecutionContext) {


  def repoScore(repo: String): Future[Option[Int]] =
    OptionT(repository.latestRatingsForRepo(repo))
      .map(weightService.weightedScore)
      .value

  def repoScoreAllRepos(): Future[Map[String, Int]] =
    for {
      indicators <- repository.latestRatings()
      scores = indicators.map(h => (h.repo, weightService.weightedScore(h))).toMap
    } yield scores


}