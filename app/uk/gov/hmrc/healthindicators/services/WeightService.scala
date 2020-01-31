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
import uk.gov.hmrc.healthindicators.configs.WeightsConfig
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WeightService @Inject()(healthIndicatorsRepository: HealthIndicatorsRepository, weightsConfig: WeightsConfig)(implicit ec: ExecutionContext) {

  def weights: Map[String, Double] = weightsConfig.weightsLookup

  def weightedScore(repo: String): Future[Option[Int]] = {
    healthIndicatorsRepository.latestIndicators(repo).map {
      case Some(x) => Some(x.ratings.map(r => applyWeight(r._type, r.calculateScore)).sum.ceil.toInt)
      case None => None
    }
  }

  def applyWeight(_type: String, score: Int): Double = {
    (weights(_type) / weightsSum()) * score
  }

  def weightsSum(): Double = {
    weights.values.sum
  }
}
