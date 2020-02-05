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

import java.time.Instant

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Collector, Collectors, HealthIndicators}
import uk.gov.hmrc.http.HeaderCarrier
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class CollectorsService @Inject()(_raters: Collectors)(implicit val ec: ExecutionContext) {

  private val raters: Seq[Collector] = _raters.raters

  def repoRatings(repo: String)(implicit hc: HeaderCarrier): Future[HealthIndicators] =
    for {
      ratings <- raters.toList.traverse(_.rate(repo))
      indicators = HealthIndicators(repo, Instant.now(), ratings)
    } yield indicators

}
