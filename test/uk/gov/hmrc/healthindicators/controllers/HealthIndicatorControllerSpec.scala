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

package uk.gov.hmrc.healthindicators.controllers

import org.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.models.{Indicator, SortType}
import uk.gov.hmrc.healthindicators.services.RepoIndicatorService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthIndicatorControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val repoIndicatorService: RepoIndicatorService = mock[RepoIndicatorService]
  private val healthIndicatorController =
    new HealthIndicatorController(repoIndicatorService, Helpers.stubControllerComponents())

  val indicator: Indicator = Indicator("repo1", Service, 100, Seq())

  val allRepoScoresAscending = Seq(
    Indicator("repo1", Service, -300, Seq.empty),
    Indicator("repo2", Service, -200, Seq.empty),
    Indicator("repo3", Service, -100, Seq.empty),
    Indicator("repo4", Service, 50, Seq.empty),
    Indicator("repo5", Service, 100, Seq.empty)
  )

  "RepoIndicatorService.indicate" should {
    "get score for individual repo" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(repoIndicatorService.indicatorForRepo("repo1"))
        .thenReturn(Future.successful(Some(indicator)))

      val result = healthIndicatorController.indicator("repo1")(fakeRequest)
      contentAsJson(result)
        .toString() shouldBe s"""{"repoName":"repo1","repoType":"Service","overallScore":100,"weightedMetrics":[]}"""
    }

    "return 404 when repo not found" in {
      val fakeRequest = FakeRequest("GET", "/repositories/repo1")
      when(repoIndicatorService.indicatorForRepo("repo1"))
        .thenReturn(Future.successful(None))

      val result = healthIndicatorController.indicator("repo1")(fakeRequest)
      status(result) shouldBe 404
    }

    "get scores for all repos in ascending order" in {
      val fakeRequest = FakeRequest("GET", "/repositories")
      when(repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Ascending))
        .thenReturn(Future.successful(allRepoScoresAscending))
      val result = healthIndicatorController.allIndicators(repoType = None, SortType.Ascending)(fakeRequest)

      contentAsJson(result) shouldBe Json.parse(
        """ |[{
         |  "repoName":"repo1",
         |  "repoType":"Service",
         |  "overallScore":-300,
         |  "weightedMetrics":[]
         |},
         |{
         |  "repoName":"repo2",
         |  "repoType":"Service",
         |  "overallScore":-200,
         |  "weightedMetrics":[]
         |},
         |{
         |  "repoName":"repo3",
         |  "repoType":"Service",
         |  "overallScore":-100,
         |  "weightedMetrics":[]
         |},
         |{
         |  "repoName":"repo4",
         |  "repoType":"Service",
         |  "overallScore":50,
         |  "weightedMetrics":[]
         |},
         |{
         |  "repoName":"repo5",
         |  "repoType":"Service",
         |  "overallScore":100,
         |  "weightedMetrics":[]
         |}]""".stripMargin
      )
    }

    "get scores for all repos in descending order, when sort equals true" in {
      val fakeRequest = FakeRequest("GET", "/repositories/")
      when(repoIndicatorService.indicatorsForAllRepos(repoType = None, SortType.Descending))
        .thenReturn(Future.successful(allRepoScoresAscending.reverse))
      val result = healthIndicatorController.allIndicators(repoType = None, SortType.Descending)(fakeRequest)

      contentAsJson(result) shouldBe Json.parse(
        """ |[{
          |  "repoName":"repo5",
          |  "repoType":"Service",
          |  "overallScore":100,
          |  "weightedMetrics":[]
          |},
          |{
          |  "repoName":"repo4",
          |  "repoType":"Service",
          |  "overallScore":50,
          |  "weightedMetrics":[]
          |},
          |{
          |  "repoName":"repo3",
          |  "repoType":"Service",
          |  "overallScore":-100,
          |  "weightedMetrics":[]
          |},
          |{
          | "repoName":"repo2",
          | "repoType":"Service",
          | "overallScore":-200,
          | "weightedMetrics":[]
          |},
          |{
          |  "repoName":"repo1",
          |  "repoType":"Service",
          |  "overallScore":-300,
          |  "weightedMetrics":[]
          |}]""".stripMargin
      )
    }
  }
}
