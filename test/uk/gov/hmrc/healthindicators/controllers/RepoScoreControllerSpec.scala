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
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.healthindicators.models.RepositoryRating
import uk.gov.hmrc.healthindicators.services.RepositoryRatingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepoScoreControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val mockWeightedRepoScorerService: RepositoryRatingService = mock[RepositoryRatingService]
  private val weightedRepoScoreController =
    new RepoScoreController(mockWeightedRepoScorerService, Helpers.stubControllerComponents())

  val repoRating: RepositoryRating = RepositoryRating("repo1", 100, Some(Seq()))

  val allRepoScores = Seq(
    RepositoryRating("repo1", 100, Some(Seq.empty)),
    RepositoryRating("repo2", 100, Some(Seq.empty)),
    RepositoryRating("repo3", 100, Some(Seq.empty)),
    RepositoryRating("repo4", 100, Some(Seq.empty)),
    RepositoryRating("repo5", 100, Some(Seq.empty))
  )

  "repoScore" should {
    "get score for individual repo" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(mockWeightedRepoScorerService.rateRepository("repo1"))
        .thenReturn(Future.successful(Some(repoRating)))

      val result = weightedRepoScoreController.scoreForRepo("repo1")(fakeRequest)
      contentAsJson(result).toString() shouldBe s"""{"repositoryName":"repo1","repositoryScore":100,"ratings":[]}"""
    }

    "get scores for all repos" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(mockWeightedRepoScorerService.rateAllRepositories())
        .thenReturn(Future.successful(allRepoScores))
      val result = weightedRepoScoreController.scoreAllRepos()(fakeRequest)
      val expectedResult =
        s"""[{"repositoryName":"repo1","repositoryScore":100,"ratings":[]},{"repositoryName":"repo2","repositoryScore":100,"ratings":[]},{"repositoryName":"repo3","repositoryScore":100,"ratings":[]},{"repositoryName":"repo4","repositoryScore":100,"ratings":[]},{"repositoryName":"repo5","repositoryScore":100,"ratings":[]}]""".stripMargin
      contentAsJson(result).toString() shouldBe expectedResult
    }
  }
}
