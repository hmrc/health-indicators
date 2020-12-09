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

import org.mockito.MockitoSugar
import org.mongodb.scala.ReadPreference
import org.mongodb.scala.model.IndexModel
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.HealthIndicators
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import cats.implicits._
import play.api.Configuration
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthIndicatorsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[HealthIndicators] {

  val config = Configuration(
    "healthindicators.refresh.enabled"      -> "false",
    "healthindicators.refresh.interval"     -> "5.minutes",
    "healthindicators.refresh.initialDelay" -> "5.minutes"
  )

  val schedulerConfigs = new SchedulerConfigs(config)

  override protected val repository = new HealthIndicatorsRepository(mongoComponent, schedulerConfigs)

  "HealthIndicatorsRepository.insert" should {

    val healthIndicators = HealthIndicators("test", Instant.now, Seq.empty)

    "insert correctly" in {
      repository.insert(healthIndicators)
      repository.findAll().futureValue mustBe Seq(healthIndicators)
    }
  }

  "HealthIndicatorsRepository.latestIndicators" should {

    val latest = HealthIndicators("test", Instant.now, Seq.empty)
    val older  = HealthIndicators("test", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val oldest = HealthIndicators("test", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest indicators for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestIndicators("test").futureValue mustBe Some(latest)
    }

    "return none if no indicators are found for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestIndicators("notfound").futureValue mustBe None
    }
  }

  "HealthIndicatorsRepository.latestIndicatorsAllRepos" should {

    val fooLatest = HealthIndicators("foo", Instant.now, Seq.empty)
    val fooOlder  = HealthIndicators("foo", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val fooOldest = HealthIndicators("foo", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    val barLatest = HealthIndicators("bar", Instant.now, Seq.empty)
    val barOlder  = HealthIndicators("bar", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val barOldest = HealthIndicators("bar", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest indicators for all repos" in {
      List(fooLatest, fooOlder, fooOldest, barLatest, barOlder, barOldest).traverse(repository.insert).futureValue
      repository.latestIndicatorsAllRepos().futureValue must contain theSameElementsAs Seq(fooLatest, barLatest)
    }
  }
}
