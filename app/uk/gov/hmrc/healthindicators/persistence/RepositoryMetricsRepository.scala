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

import org.mongodb.scala.bson.conversions
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.connectors.RepoType
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepositoryMetricsRepository @Inject() (
  mongoComponent: MongoComponent,
  config: SchedulerConfigs
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[RepositoryMetrics](
      collectionName = "repositoryMetrics",
      mongoComponent = mongoComponent,
      domainFormat = RepositoryMetrics.mongoFormats,
      indexes = Seq(
        IndexModel(hashed("repoName"), IndexOptions().background(true)),
        IndexModel(descending("timestamp"), IndexOptions().background(true)),
        IndexModel(hashed("repoType"), IndexOptions().background(true))
      )
    ) {

  def latestRepositoryMetrics(repo: String): Future[Option[RepositoryMetrics]] =
    collection
      .find(equal("repoName", repo))
      .sort(descending("timestamp"))
      .headOption()

  private def createPipeline(repoType: Option[RepoType]): List[conversions.Bson] = {
    val getLatest = List(
      sort(descending("timestamp")),
      group("$repoName", first("obj", "$$ROOT")),
      replaceRoot("$obj")
    )

    repoType match {
      case Some(rt) => `match`(equal("repoType", rt.toString)) +: getLatest
      case None     => getLatest
    }
  }

  def allLatestRepositoryMetrics(repoType: Option[RepoType]): Future[Seq[RepositoryMetrics]] =
    collection
      .aggregate(createPipeline(repoType))
      .toFuture()

  def insert(metrics: RepositoryMetrics): Future[Unit] =
    collection
      .insertOne(
        metrics
      )
      .toFuture()
      .map(_ => ())

  def findAll(): Future[Seq[RepositoryMetrics]] =
    collection.find().toFuture().map(_.toList)
}
