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

package uk.gov.hmrc.healthindicators.persistence

import javax.inject.Inject
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.models.RepositoryHealthIndicator
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

class HealthIndicatorsRepository @Inject() (
  mongoComponent: MongoComponent,
  config: SchedulerConfigs
                                           //TODO: is config actually used?
  //Todo: Function that retrieves in an order based on score?
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[RepositoryHealthIndicator](
      collectionName = "serviceHealthIndicators",
      mongoComponent = mongoComponent,
      domainFormat = RepositoryHealthIndicator.mongoFormats,
      indexes = Seq(
        IndexModel(hashed("repositoryName"), IndexOptions().background(true)),
        IndexModel(descending("timestamp"), IndexOptions().background(true))
      )
    ) {

  def latestRepositoryHealthIndicators(repo: String): Future[Option[RepositoryHealthIndicator]] =
    collection
      .find(equal("repositoryName", repo))
      .sort(descending("timestamp"))
      .headOption()

    def latestAllRepositoryHealthIndicators(): Future[Seq[RepositoryHealthIndicator]] = {
      val agg = List(
        sort(descending("timestamp")),
        group("$repositoryName", first("obj", "$$ROOT")),
        replaceRoot("$obj")
      )
      collection
        .aggregate(agg)
        .toFuture()
    }

  def insert(indicator: RepositoryHealthIndicator): Future[Unit] =
    collection
      .insertOne(
        indicator
      )
      .toFuture()
      .map(_ => ())

  def findAll(): Future[Seq[RepositoryHealthIndicator]] =
    collection.find().toFuture().map(_.toList)
}
