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

package uk.gov.hmrc.healthindicators.controllers

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}

import scala.concurrent.duration._
import uk.gov.hmrc.healthindicators.models.RepoScoreBreakdown
import uk.gov.hmrc.healthindicators.services.RepoScorerService
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

class RepoScoreControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockWeightedRepoScorerService: RepoScorerService = mock[RepoScorerService]
  private val weightedRepoScoreController =
    new RepoScoreController(mockWeightedRepoScorerService, Helpers.stubControllerComponents())

  val repoBreakDown1: RepoScoreBreakdown = RepoScoreBreakdown("repo1", 100, Seq())

  val allRepoScores = Map("repo1" -> 100, "repo2" -> 100, "repo3" -> 100, "repo4" -> 100, "repo5" -> 100)

  "WeightedRepoScoreController.scoreForRepo" should {
    "get score for individual repo" in {
      val fakeRequest = FakeRequest("GET", "/api/health-score/repositories/repo1")
      when(mockWeightedRepoScorerService.repoScore("repo1"))
        .thenReturn(Future.successful(Some(repoBreakDown1)))

      val result = weightedRepoScoreController.scoreForRepo("repo1")(fakeRequest)
      contentAsJson(result).toString() shouldBe s"""{"repo":"repo1","weightedScore":100,"ratings":[]}"""
    }

    "get scores for all repos" in {
      val fakeRequest = FakeRequest("GET", "/api/health-score/repositories/repo1")
      when(mockWeightedRepoScorerService.repoScoreAllRepos())
        .thenReturn(Future.successful(allRepoScores))
      val result         = weightedRepoScoreController.scoreAllRepos()(fakeRequest)
      val expectedResult = s"""{"repo2":100,"repo4":100,"repo5":100,"repo1":100,"repo3":100}"""
      contentAsJson(result).toString() shouldBe expectedResult
    }
  }
}
