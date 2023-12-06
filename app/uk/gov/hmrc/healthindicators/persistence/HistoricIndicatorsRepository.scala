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
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.models.HistoricIndicator
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HistoricIndicatorsRepository @Inject() (
  mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[HistoricIndicator](
      collectionName = "historicHealthIndicators",
      mongoComponent = mongoComponent,
      domainFormat = HistoricIndicator.format,
      indexes = Seq(
        IndexModel(hashed("repoName"), IndexOptions().background(true)),
        IndexModel(descending("timestamp"), IndexOptions().background(true))
      )
    ) {

  override lazy val requiresTtlIndex: Boolean = false // we want to accumulate historic data

  def insert(historicIndicator: HistoricIndicator): Future[Unit] =
    collection
      .insertOne(
        historicIndicator
      )
      .toFuture()
      .map(_ => ())

  def insert(historicIndicators: Seq[HistoricIndicator]): Future[Unit] =
    collection
      .insertMany(
        historicIndicators
      )
      .toFuture()
      .map(_ => ())

  def findAll: Future[Seq[HistoricIndicator]] =
    collection
      .find()
      .toFuture()
      .map(_.toList)

  def findAllForRepo(repoName: String): Future[Seq[HistoricIndicator]] =
    collection
      .find(equal("repoName", repoName))
      .sort(descending("timestamp"))
      .toFuture()
}
