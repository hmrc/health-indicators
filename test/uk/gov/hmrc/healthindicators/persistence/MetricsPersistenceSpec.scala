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
import uk.gov.hmrc.healthindicators.connectors.RepoType.{Prototype, Service}
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class MetricsPersistenceSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[RepositoryMetrics] {

  private val config: Configuration = Configuration(
    "metrics.refresh.enabled"      -> "false",
    "metrics.refresh.interval"     -> "5.minutes",
    "metrics.refresh.initialDelay" -> "5.minutes"
  )

  private val schedulerConfigs = new SchedulerConfigs(config)

  override protected val repository = new MetricsPersistence(mongoComponent, schedulerConfigs)

  "insert" should {

    val metrics = RepositoryMetrics("test", Instant.now, Service, Seq.empty)

    "insert correctly" in {
      repository.insert(metrics)
      repository.findAll().futureValue must contain(metrics)
    }
  }

  "latestRepositoryMetrics" should {

    val latest = RepositoryMetrics("test", Instant.now, Service, Seq.empty)
    val older  = RepositoryMetrics("test", Instant.now.minus(1, ChronoUnit.DAYS), Service, Seq.empty)
    val oldest = RepositoryMetrics("test", Instant.now.minus(2, ChronoUnit.DAYS), Service, Seq.empty)

    "return the latest metrics for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRepositoryMetrics("test").futureValue mustBe Some(latest)
    }

    "return none if no metrics are found for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRepositoryMetrics("notfound").futureValue mustBe None
    }
  }

  "MetricsPersistence.allLatestRepositoryMetrics" should {

    val fooLatest = RepositoryMetrics("foo", Instant.now, Service, Seq.empty)
    val fooOlder  = RepositoryMetrics("foo", Instant.now.minus(1, ChronoUnit.DAYS), Service, Seq.empty)
    val fooOldest = RepositoryMetrics("foo", Instant.now.minus(2, ChronoUnit.DAYS), Service, Seq.empty)

    val barLatest = RepositoryMetrics("bar", Instant.now, Prototype, Seq.empty)
    val barOlder  = RepositoryMetrics("bar", Instant.now.minus(1, ChronoUnit.DAYS), Prototype, Seq.empty)
    val barOldest = RepositoryMetrics("bar", Instant.now.minus(2, ChronoUnit.DAYS), Prototype, Seq.empty)

    "return the latest metrics for all repos when no filter is applied" in {
      List(fooLatest, fooOlder, fooOldest, barLatest, barOlder, barOldest).traverse(repository.insert).futureValue
      repository.allLatestRepositoryMetrics(repoType = None).futureValue must contain theSameElementsAs Seq(
        fooLatest,
        barLatest
      )
    }

    "returns empty list when no results are found" in {
      repository.allLatestRepositoryMetrics(repoType = None).futureValue mustBe List()
    }

    "returns only one result when there is a duplicate" in {
      List(fooLatest, fooLatest).traverse(repository.insert).futureValue
      repository.allLatestRepositoryMetrics(repoType = None).futureValue mustBe List(fooLatest)
    }

    "returns only Services when filtered by Service" in {
      List(fooLatest, barLatest).traverse(repository.insert).futureValue
      repository.allLatestRepositoryMetrics(Some(Service)).futureValue mustBe List(fooLatest)
    }

    "returns only Prototypes when filtered by Prototype" in {
      List(fooLatest, barLatest).traverse(repository.insert).futureValue
      repository.allLatestRepositoryMetrics(Some(Prototype)).futureValue mustBe List(barLatest)
    }
  }
}
