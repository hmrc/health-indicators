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
import cats.implicits._
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.connectors.RepositoryType.{Prototype, Service}
import uk.gov.hmrc.healthindicators.models.RepositoryHealthIndicator
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class HealthIndicatorsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[RepositoryHealthIndicator] {

  private val config: Configuration = Configuration(
    "reporatings.refresh.enabled"      -> "false",
    "reporatings.refresh.interval"     -> "5.minutes",
    "reporatings.refresh.initialDelay" -> "5.minutes"
  )

  private val schedulerConfigs = new SchedulerConfigs(config)

  override protected val repository = new HealthIndicatorsRepository(mongoComponent, schedulerConfigs)

  "insert" should {

    val healthIndicator = RepositoryHealthIndicator("test", Instant.now, Service, Seq.empty)

    "insert correctly" in {
      repository.insert(healthIndicator)
      repository.findAll().futureValue must contain(healthIndicator)
    }
  }

  "latestRepoRatings" should {

    val latest = RepositoryHealthIndicator("test", Instant.now, Service, Seq.empty)
    val older  = RepositoryHealthIndicator("test", Instant.now.minus(1, ChronoUnit.DAYS), Service, Seq.empty)
    val oldest = RepositoryHealthIndicator("test", Instant.now.minus(2, ChronoUnit.DAYS), Service, Seq.empty)

    "return the latest repoRatings for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRepositoryHealthIndicators("test").futureValue mustBe Some(latest)
    }

    "return none if no repoRatings are found for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRepositoryHealthIndicators("notfound").futureValue mustBe None
    }
  }

  "RepoRatingsPersistence.latestRepoRatingsAllRepos" should {

    val fooLatest = RepositoryHealthIndicator("foo", Instant.now, Service, Seq.empty)
    val fooOlder  = RepositoryHealthIndicator("foo", Instant.now.minus(1, ChronoUnit.DAYS), Service, Seq.empty)
    val fooOldest = RepositoryHealthIndicator("foo", Instant.now.minus(2, ChronoUnit.DAYS), Service,Seq.empty)

    val barLatest = RepositoryHealthIndicator("bar", Instant.now, Prototype, Seq.empty)
    val barOlder  = RepositoryHealthIndicator("bar", Instant.now.minus(1, ChronoUnit.DAYS), Prototype, Seq.empty)
    val barOldest = RepositoryHealthIndicator("bar", Instant.now.minus(2, ChronoUnit.DAYS), Prototype, Seq.empty)

    "return the latest repoRatings for all repos when no filter is applied" in {
      List(fooLatest, fooOlder, fooOldest, barLatest, barOlder, barOldest).traverse(repository.insert).futureValue
      repository.latestAllRepositoryHealthIndicators(None).futureValue must contain theSameElementsAs Seq(
        fooLatest,
        barLatest
      )
    }

    "returns empty list when no results are found" in {
      repository.latestAllRepositoryHealthIndicators(None).futureValue mustBe List()
    }

    "returns only one result when there is a duplicate" in {
      List(fooLatest, fooLatest).traverse(repository.insert).futureValue
      repository.latestAllRepositoryHealthIndicators(None).futureValue mustBe List(fooLatest)
    }

    "returns only Services when filtered by Service" in {
      List(fooLatest, barLatest).traverse(repository.insert).futureValue
      repository.latestAllRepositoryHealthIndicators(Some("Service")).futureValue mustBe List(fooLatest)
    }

    "returns only Prototypes when filtered by Prototype" in {
      List(fooLatest, barLatest).traverse(repository.insert).futureValue
      repository.latestAllRepositoryHealthIndicators(Some("Prototype")).futureValue mustBe List(barLatest)
    }

    "returns no result when filtered by incorrect repoType" in {
      List(fooLatest, barLatest).traverse(repository.insert).futureValue
      repository.latestAllRepositoryHealthIndicators(Some("blah")).futureValue mustBe List()
    }
  }
}
