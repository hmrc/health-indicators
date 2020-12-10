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

import cats.implicits._
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.models.RepoRatings
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class TeamsAndReposRatingsRepoSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[RepoRatings] {

  val config = Configuration(
    "reporatings.refresh.enabled"      -> "false",
    "reporatings.refresh.interval"     -> "5.minutes",
    "reporatings.refresh.initialDelay" -> "5.minutes"
  )

  val schedulerConfigs = new SchedulerConfigs(config)

  override protected val repository = new RepoRatingsPersistence(mongoComponent, schedulerConfigs)

  "RepoRatingsPersistence.insert" should {

    val repoRatings = RepoRatings("test", Instant.now, Seq.empty)

    "insert correctly" in {
      repository.insert(repoRatings)
      repository.findAll().futureValue must contain(repoRatings)
    }
  }

  "RepoRatingsPersistence.latestRepoRatings" should {

    val latest = RepoRatings("test", Instant.now, Seq.empty)
    val older  = RepoRatings("test", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val oldest = RepoRatings("test", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest repoRatings for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRatingsForRepo("test").futureValue mustBe Some(latest)
    }

    "return none if no repoRatings are found for repo" in {
      List(latest, older, oldest).traverse(repository.insert).futureValue
      repository.latestRatingsForRepo("notfound").futureValue mustBe None
    }
  }

  "RepoRatingsPersistence.latestRepoRatingsAllRepos" should {

    val fooLatest = RepoRatings("foo", Instant.now, Seq.empty)
    val fooOlder  = RepoRatings("foo", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val fooOldest = RepoRatings("foo", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    val barLatest = RepoRatings("bar", Instant.now, Seq.empty)
    val barOlder  = RepoRatings("bar", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val barOldest = RepoRatings("bar", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest repoRatings for all repos" in {
      List(fooLatest, fooOlder, fooOldest, barLatest, barOlder, barOldest).traverse(repository.insert).futureValue
      repository.latestRatings().futureValue must contain theSameElementsAs Seq(fooLatest, barLatest)
    }
  }
}
