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

package uk.gov.hmrc.healthindicators.persistence

import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import uk.gov.hmrc.healthindicators.connectors.RepoType
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RepositoryMetricsRepository @Inject() (
  mongoComponent: MongoComponent
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

  override lazy val requiresTtlIndex: Boolean = false // records are replaced on a schedule

  def getRepositoryMetrics(repo: String): Future[Option[RepositoryMetrics]] =
    collection
      .find(equal("repoName", repo))
      .headOption()

  def getAllRepositoryMetrics(repoType: Option[RepoType]): Future[Seq[RepositoryMetrics]] =
    repoType match {
      case Some(rt) => collection.find(equal("repoType", rt.asString)).toFuture()
      case None     => findAll()
    }

  def insert(repo: String, metrics: RepositoryMetrics): Future[Unit] =
    collection
      .replaceOne(
        filter = equal("repoName", repo),
        replacement = metrics,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())

  def findAll(): Future[Seq[RepositoryMetrics]] =
    collection
      .find()
      .toFuture()
      .map(_.toList)
}
