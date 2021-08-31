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

import org.mongodb.scala.model.Indexes._
import org.mongodb.scala.model.{IndexModel, IndexOptions}
import uk.gov.hmrc.healthindicators.models.AveragePlatformScore
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AveragePlatformScoreRepository @Inject() (
                                               mongoComponent: MongoComponent,
                                             )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[AveragePlatformScore](
    collectionName = "averagePlatformScores",
    mongoComponent = mongoComponent,
    domainFormat = AveragePlatformScore.format,
    indexes = Seq(
      IndexModel(descending("timestamp"), IndexOptions().background(true))
    )
  ) {

  def insert(averagePlatformScore: AveragePlatformScore): Future[Unit] =
    collection
      .insertOne(
        averagePlatformScore
      )
      .toFuture()
      .map(_ => ())


  def findLatest: Future[Option[AveragePlatformScore]] =
    collection
      .find()
      .sort(descending("timestamp"))
      .headOption()



  def findAll(): Future[Seq[AveragePlatformScore]] =
    collection
      .find()
      .sort(descending("timestamp"))
      .toFuture()


}
