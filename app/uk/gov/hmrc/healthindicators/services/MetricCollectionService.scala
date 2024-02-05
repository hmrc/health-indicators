/*
 * Copyright 2023 HM Revenue & Customs
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

import cats.implicits._
import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.{TeamsAndRepos, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.metricproducers.MetricProducer
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MetricCollectionService @Inject() (
  teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector,
  metricProducers              : List[MetricProducer],
  repository                   : RepositoryMetricsRepository
)(implicit ec: ExecutionContext) {

  private val logger = Logger(this.getClass)

  def collectAll()(implicit hc: HeaderCarrier): Future[Unit] =
    for {
      repos <- teamsAndRepositoriesConnector.allRepositories
      _     <- repos.foldLeftM(())((_, r) =>
                 for {
                   indicators <- createMetricsForRepo(r)
                   _          <- repository.insert(indicators.repoName, indicators)
                 } yield ()
               )
    } yield ()

  private def createMetricsForRepo(repo: TeamsAndRepos): Future[RepositoryMetrics] =
    for {
      _       <- Future.successful(logger.info(s"Creating Metrics For: $repo"))
      metrics <- metricProducers.traverse(_.produce(repo.name))
    } yield RepositoryMetrics(repo.name, Instant.now(), repo.repositoryType, metrics)
}
