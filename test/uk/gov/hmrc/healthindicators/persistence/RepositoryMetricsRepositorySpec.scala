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

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.RepoType.{Prototype, Service}
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class RepositoryMetricsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[RepositoryMetrics] {

  override protected val repository = new RepositoryMetricsRepository(mongoComponent)

  private val now = Instant.now.truncatedTo(ChronoUnit.MILLIS)

  "insert" should {
    val metrics    = RepositoryMetrics("test", now,                          Service, Seq.empty)
    val metricsTwo = RepositoryMetrics("test", now.plus(1, ChronoUnit.DAYS), Service, Seq.empty)

    "insert correctly" in {
      repository.insert(metrics.repoName, metrics).futureValue
      repository.findAll().futureValue should contain(metrics)
    }

    "replace correctly" in {
      repository.insert(metrics.repoName, metrics).futureValue
      repository.insert(metrics.repoName, metricsTwo).futureValue
      repository.findAll().futureValue should contain(metricsTwo)
    }
  }

  "getRepositoryMetrics" should {
    val foo = RepositoryMetrics("foo", now, Service, Seq.empty)

    "return metrics for repo" in {
      repository.insert(foo.repoName, foo).futureValue
      repository.getRepositoryMetrics(foo.repoName).futureValue shouldBe Some(foo)
    }

    "return none if no metrics are found for repo" in {
      repository.insert(foo.repoName, foo).futureValue
      repository.getRepositoryMetrics("notfound").futureValue shouldBe None
    }
  }

  "MetricsPersistence.findAll" should {
    val foo = RepositoryMetrics("foo", now, Service  , Seq.empty)
    val bar = RepositoryMetrics("bar", now, Prototype, Seq.empty)

    "return the metrics for all repos when no filter is applied" in {
      repository.insert(foo.repoName, foo).futureValue
      repository.insert(bar.repoName, bar).futureValue
      repository.findAll(repoType = None).futureValue should contain theSameElementsAs Seq(
        foo,
        bar
      )
    }

    "returns empty list when no results are found" in {
      repository.findAll(repoType = None).futureValue shouldBe Seq.empty
    }

    "returns only Services when filtered by Service" in {
      repository.insert(foo.repoName, foo).futureValue
      repository.insert(bar.repoName, bar).futureValue
      repository.findAll(Some(Service)).futureValue shouldBe Seq(foo)
    }

    "returns only Prototypes when filtered by Prototype" in {
      repository.insert(foo.repoName, foo).futureValue
      repository.insert(bar.repoName, bar).futureValue
      repository.findAll(Some(Prototype)).futureValue shouldBe Seq(bar)
    }
  }
}
