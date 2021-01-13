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

import java.time.Instant
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.{equal, gt}
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.models.RepoRatings
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import scala.concurrent.{ExecutionContext, Future}

class RepoRatingsPersistence @Inject() (
  mongoComponent: MongoComponent,
  config: SchedulerConfigs
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[RepoRatings](
      collectionName = "repoRatings",
      mongoComponent = mongoComponent,
      domainFormat = RepoRatings.mongoFormats,
      indexes = Seq(
        IndexModel(hashed("repo"), IndexOptions().background(true)),
        IndexModel(descending("date"), IndexOptions().background(true))
      )
    ) {

  def latestRatingsForRepo(repo: String): Future[Option[RepoRatings]] =
    collection
      .find(equal("repo", repo))
      .sort(descending("date"))
      .toFuture()
      .map(_.headOption)

  def latestRatings(): Future[Seq[RepoRatings]] = {
    val agg = List(
      `match`(gt("date", Instant.now.minus(2 * config.repoRatingsScheduler.frequency().toMillis, ChronoUnit.MILLIS))),
      sort(descending("date")),
      group("$repo", first("obj", "$$ROOT")),
      replaceRoot("$obj")
    )

    collection
      .aggregate(agg)
      .toFuture()
  }

  def insert(repoRatings: RepoRatings): Future[Unit] =
    collection
      .insertOne(
        repoRatings
      )
      .toFuture()
      .map(_ => ())

  def findAll(): Future[Seq[RepoRatings]] =
    collection.find().toFuture().map(_.toList)
}
