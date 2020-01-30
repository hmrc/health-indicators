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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.healthindicators.model.Rating
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WeightService @Inject()(healthIndicatorsRepository: HealthIndicatorsRepository)(implicit ec: ExecutionContext) {

  def weightedScore(repo: String): Future[Option[Int]] = {
    healthIndicatorsRepository.latestIndicators(repo).map {
      case Some(x) => Some(x.ratings.map(r => WeightService.applyWeight(r)).sum.ceil.toInt)
      case None => None
    }
  }
}

object WeightService {

  val weightLookup = Map(
    "ReadMeRating" -> 2.0
  , "LeakDetectionRating" -> 1.0
  )

  def applyWeight(rating: Rating): Double = {
    (weightLookup(rating._type) / weightsSum()) * rating.rating
  }

  def weightsSum(): Double = {
    weightLookup.values.sum
  }
}
