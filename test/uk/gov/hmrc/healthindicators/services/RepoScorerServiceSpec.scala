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

package uk.gov.hmrc.healthindicators.services

import java.time.Instant

import org.joda.time.DurationFieldType.seconds
import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.models.{RatingType, RepoRatings, RepoScoreBreakdown}
import uk.gov.hmrc.healthindicators.persistence.RepoRatingsPersistence
import uk.gov.hmrc.healthindicators.raters.leakdetection.LeakDetectionRating
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeRating
import uk.gov.hmrc.healthindicators.raters.bobbyrules.BobbyRulesRating
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeType.ValidReadMe

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class RepoScorerServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockRepository = mock[RepoRatingsPersistence]
  val bobbyRulesRating = BobbyRulesRating(1, 1)
  val readMeRating = ReadMeRating(ValidReadMe)
  val leakDetectionRating = LeakDetectionRating(1)

  val ratings = RepoRatings("repo1", Instant.now(), Seq(bobbyRulesRating, readMeRating, leakDetectionRating))

  val scoreConfig = new ScoreConfig
  private val repoScorerService = new RepoScorerService(mockRepository, scoreConfig)

  "repoScore" should {

    "Return a Total Score based on Ratings and Weights" in {

      val repoBreakDown = RepoScoreBreakdown("foo", -70, Seq(bobbyRulesRating,
        readMeRating, leakDetectionRating))

      when(mockRepository.latestRatingsForRepo("foo"))
        .thenReturn(Future.successful(Some(ratings)))

      val result  = repoScorerService.repoScore("foo")
      Await.result(result, 5 seconds) mustBe Some(repoBreakDown)
    }
  }
}
