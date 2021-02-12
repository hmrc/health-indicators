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

package uk.gov.hmrc.healthindicators

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.{GuiceOneAppPerSuite, GuiceOneServerPerSuite}
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.test.FakeRequest
import uk.gov.hmrc.healthindicators.configs.SchedulerConfigs
import uk.gov.hmrc.healthindicators.models.RepositoryHealthIndicator
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class IntegrationSpec
  extends AnyWordSpec
    with DefaultPlayMongoRepositorySupport[RepositoryHealthIndicator]
  with GuiceOneServerPerSuite
  with WireMockEndpoints
  with Matchers
  with ScalaFutures
  {

  private val config: Configuration = Configuration(
    "reporatings.refresh.enabled"      -> "true",
    "reporatings.refresh.interval"     -> "5.minutes",
    "reporatings.refresh.initialDelay" -> "3.seconds"
  )

  private val schedulerConfigs = new SchedulerConfigs(config)

  protected val repository = new HealthIndicatorsRepository(mongoComponent, schedulerConfigs)

  private[this] lazy val ws = app.injector.instanceOf[WSClient]

  override def fakeApplication: Application =
    new GuiceApplicationBuilder()
      .disable(classOf[com.kenshoo.play.metrics.PlayModule])
      .configure(
        Map(
          "mongodb.uri" -> mongoUri,
          "reporatings.refresh.enabled"      -> "true",
          "reporatings.refresh.interval"     -> "5.minutes",
          "reporatings.refresh.initialDelay" -> "1.seconds",
          "microservice.services.service-dependencies.port" -> endpointPort,
          "microservice.services.service-dependencies.host" -> host,
          "microservice.services.leak-detection.port" -> endpointPort,
          "microservice.services.leak-detection.host" -> host,
          "github.open.api.rawurl" -> endpointMockUrl,
          "github.open.api.token" -> "test-token",
          "microservice.services.teams-and-repositories.port" -> endpointPort,
          "microservice.services.teams-and-repositories.host" -> host,
          "metrics.jvm" -> false
        )
      )
      .build()

  "Service Health Indicators" should {
    "return 200 when it starts correctly and receives GET /ping/ping" in {
      val response = ws.url(s"http://localhost:$port/ping/ping").get.futureValue

      response.status shouldBe 200
    }

    "return correct json when scoreForRepo receives a get request with valid repo name" in {

      serviceEndpoint(GET, "/api/repositories", willRespondWith = (200, Some(teamsAndReposJson)))
      serviceEndpoint(GET, "/api/dependencies/auth", willRespondWith = (200, Some(serviceDependenciesJson)))
      serviceEndpoint(GET, "/api/reports/repositories/auth", willRespondWith = (200, Some(leakDetectionJson)))
      serviceEndpoint(GET, "/hmrc/auth/master/README.md",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (404, None))

      val response = ws.url(s"http://localhost:$port/repositories/auth").get.futureValue

      response.status shouldBe 200
      eventually{ response.body shouldBe expectedResponse }
    }
  }

  val leakDetectionJson =
    """
        {
         "_id": "123",
         "inspectionResults": [
           {
            "filePath": "/this/is/a/test",
            "scope": "fileName",
            "lineNumber": 1,
            "urlToSource": "https://test-url",
            "ruleId": "filename_test",
            "description": "test123",
            "lineText": "test.text"
           }
         ]
      }"""

  val serviceDependenciesJson = """{
    "repositoryName": "auth",
    "libraryDependencies": [
    {
      "name": "simple-reactivemongo",
      "group": "uk.gov.hmrc",
      "currentVersion": {
        "major": 7,
        "minor": 30,
        "patch": 0,
        "original": "7.30.0-play-26"
       },
      "latestVersion": {
        "major": 7,
        "minor": 31,
        "patch": 0,
        "original": "7.31.0-play-26"
      },
      "bobbyRuleViolations": [
        {
          "reason": "TEST DEPRECATION",
          "from": "2050-05-01",
          "range": "(,99.99.99)"
        }
      ],
      "isExternal": false
      }
    ],
    "sbtPluginsDependencies": [],
    "otherDependencies": [],
    "lastUpdated": "2020-12-07T11:11:53.122Z"
            }"""

  val teamsAndReposJson =
   """
    [{
      "name": "auth",
      "createdAt": 1541588042000,
      "lastUpdatedAt": 1601630778000,
      "repoType": "Prototype",
      "language": "HTML",
      "archived": false
    }]
  """

    val expectedResponse =
      """{"repositoryName":"auth","repositoryScore":-120,"ratings":[{"ratingType":"BobbyRule","ratingScore":-20,"breakdown":[{"points":-20,"description":"simple-reactivemongo - TEST DEPRECATION"}]},{"ratingType":"LeakDetection","ratingScore":-50,"breakdown":[{"points":-50,"description":"test123","ratings":"https://test-url"}]},{"ratingType":"ReadMe","ratingScore":-50,"breakdown":[{"points":-50,"description":"No Readme defined"}]}]}"""
}
