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

import javax.inject.Inject
import org.mongodb.scala.Completed
import uk.gov.hmrc.healthindicators.connectors.TeamsAndRepositoriesConnector
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class HealthIndicatorsService @Inject()(
    repository: HealthIndicatorsRepository
  , teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector
  , ratingsService: RatingsService
  )(implicit val ec: ExecutionContext) {

  def insertRatings()(implicit hc: HeaderCarrier): Future[Seq[Completed]] = {

    for {
      repos <- teamsAndRepositoriesConnector.allRepositories
      ratings <- Future.sequence(repos.map(r => ratingsService.repoRatings(r.name)))
      insert <- repository.insert(ratings)
    } yield insert

  }

}
