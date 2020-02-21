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

package uk.gov.hmrc.healthindicators.persistence

import java.time.Instant
import java.time.temporal.ChronoUnit

import javax.inject.Inject
import org.mongodb.scala.Completed
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters.{equal, gt}
import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.models.HealthIndicators
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoCollection
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import scala.concurrent.{ExecutionContext, Future}

class HealthIndicatorsRepository @Inject()(
  mongoComponent: MongoComponent
)(implicit ec: ExecutionContext)
    extends PlayMongoCollection[HealthIndicators](
      collectionName = "healthIndicators",
      mongoComponent = mongoComponent,
      domainFormat   = HealthIndicators.mongoFormats,
      indexes = Seq(
        IndexModel(hashed("repo"), IndexOptions().background(true)),
        IndexModel(descending("date"), IndexOptions().background(true)))
    ) {

  def latestIndicators(repo: String): Future[Option[HealthIndicators]] =
    collection
      .find(equal("repo", repo))
      .sort(descending("date"))
      .toFuture()
      .map(_.headOption)

  def latestIndicatorsAllRepos(): Future[Seq[HealthIndicators]] = {
    val agg = List(
      `match`(gt("date", Instant.now.minus(48, ChronoUnit.HOURS))),
      sort(descending("date")),
      group("$repo", first("obj", "$$ROOT")),
      replaceRoot("$obj")
    )

    collection
      .aggregate(agg)
      .toFuture()
  }

  def insertOne(healthIndicators: HealthIndicators): Future[Completed] =
    collection
      .insertOne(
        healthIndicators
      )
      .toFuture()

  def insert(seqHealthIndicators: Seq[HealthIndicators]): Future[Seq[Completed]] =
    Future.traverse(seqHealthIndicators)(insertOne)
}
