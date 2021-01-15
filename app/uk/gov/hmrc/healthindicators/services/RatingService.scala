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

import java.time.Instant

import cats.implicits._
import com.google.inject.Injector
import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.TeamsAndRepositoriesConnector
import uk.gov.hmrc.healthindicators.models.ServiceHealthIndicator
import uk.gov.hmrc.healthindicators.persistence.RepoRatingsPersistence
import uk.gov.hmrc.healthindicators.raters.Rater
import uk.gov.hmrc.http.HeaderCarrier

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class RatingService @Inject() (
  teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
  repository: RepoRatingsPersistence,
  inject: Injector
)(implicit val ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  private def repoRatings(repo: String): Future[ServiceHealthIndicator] = {
    logger.info(s"Rating Repository: $repo")
    for {
      indicators <- raters().traverse(_.rate(repo))
    } yield
      ServiceHealthIndicator(repo, Instant.now(), indicators.flatten)
  }

  def insertRatings()(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      repos <- teamsAndRepositoriesConnector.allRepositories
      _ <- repos.foldLeftM(())((_, r) =>
             for {
               indicators <- repoRatings(r.name)
               _          <- repository.insert(indicators)
             } yield ()
           )
    } yield ()

  private def raters(): List[Rater] = inject.getAllBindings.keySet().asScala
    .filter(k => classOf[Rater].isAssignableFrom(k.getTypeLiteral.getRawType))
    .map(k => inject.getInstance(k).asInstanceOf[Rater])
    .toList

}
