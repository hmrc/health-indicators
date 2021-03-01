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
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.healthindicators.models.{RepositoryRating, SortType}
import uk.gov.hmrc.healthindicators.services.RepositoryRatingService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepoScoreControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val repoScorerService: RepositoryRatingService = mock[RepositoryRatingService]
  private val repoScoreController =
    new RepoScoreController(repoScorerService, Helpers.stubControllerComponents())

  val repoRating: RepositoryRating = RepositoryRating("repo1", 100, Some(Seq()))

  val allRepoScoresAscending = Seq(
    RepositoryRating("repo1", -300, Some(Seq.empty)),
    RepositoryRating("repo2", -200, Some(Seq.empty)),
    RepositoryRating("repo3", -100, Some(Seq.empty)),
    RepositoryRating("repo4",   50, Some(Seq.empty)),
    RepositoryRating("repo5",  100, Some(Seq.empty))
  )

  "repoScore" should {
    "get score for individual repo" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(repoScorerService.rateRepository("repo1"))
        .thenReturn(Future.successful(Some(repoRating)))

      val result = repoScoreController.scoreForRepo("repo1")(fakeRequest)
      contentAsJson(result).toString() shouldBe s"""{"repositoryName":"repo1","repositoryScore":100,"ratings":[]}"""
    }

    "return 404 when repo not found" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(repoScorerService.rateRepository("repo1"))
        .thenReturn(Future.successful(None))

       val result = repoScoreController.scoreForRepo("repo1")(fakeRequest)
      status(result) shouldBe 404
    }

    "get scores for all repos in ascending order" in {
      val fakeRequest = FakeRequest("GET", "/repositories")
      when(repoScorerService.rateAllRepositories(SortType.Ascending))
        .thenReturn(Future.successful(allRepoScoresAscending))
      val result = repoScoreController.scoreAllRepos(SortType.Ascending)(fakeRequest)


     contentAsJson(result) shouldBe Json.parse(
     """ |[{
         |  "repositoryName":"repo1",
         |  "repositoryScore":-300,
         |  "ratings":[]
         |},
         |{
         |  "repositoryName":"repo2",
         |  "repositoryScore":-200,
         |  "ratings":[]
         |},
         |{
         |  "repositoryName":"repo3",
         |  "repositoryScore":-100,
         |  "ratings":[]
         |},
         |{
         |  "repositoryName":"repo4",
         |  "repositoryScore":50,
         |  "ratings":[]
         |},
         |{
         |  "repositoryName":"repo5",
         |  "repositoryScore":100,
         |  "ratings":[]
         |}]""".stripMargin
     )
    }

    "get scores for all repos in descending order, when sort equals true" in {
      val fakeRequest = FakeRequest("GET", "/repositories/")
      when(repoScorerService.rateAllRepositories(SortType.Descending))
        .thenReturn(Future.successful(allRepoScoresAscending.reverse))
      val result = repoScoreController.scoreAllRepos(SortType.Descending)(fakeRequest)


      contentAsJson(result) shouldBe Json.parse(
      """ |[{
          |  "repositoryName":"repo5",
          |  "repositoryScore":100,
          |  "ratings":[]
          |},
          |{
          |  "repositoryName":"repo4",
          |  "repositoryScore":50,
          |  "ratings":[]
          |},
          |{
          |  "repositoryName":"repo3",
          |  "repositoryScore":-100,
          |  "ratings":[]
          |},
          |{
          | "repositoryName":"repo2",
          | "repositoryScore":-200,
          | "ratings":[]
          |},
          |{
          |  "repositoryName":"repo1",
          |  "repositoryScore":-300,
          |  "ratings":[]
          |}]""".stripMargin
      )
    }
  }
}
