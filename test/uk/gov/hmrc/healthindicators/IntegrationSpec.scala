/*
 * Copyright 2022 HM Revenue & Customs
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
import com.google.common.io.BaseEncoding
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.Application
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class IntegrationSpec
    extends AnyWordSpec
    with DefaultPlayMongoRepositorySupport[RepositoryMetrics]
    with GuiceOneServerPerSuite
    with WireMockEndpoints
    with Matchers
    with ScalaFutures
    with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1000, Millis)))

  protected val repository: RepositoryMetricsRepository = app.injector.instanceOf[RepositoryMetricsRepository]
  private[this] lazy val ws                            = app.injector.instanceOf[WSClient]

  override def fakeApplication: Application =
    new GuiceApplicationBuilder()
      .disable(classOf[com.kenshoo.play.metrics.PlayModule])
      .configure(
        Map(
          "mongodb.uri"                                       -> mongoUri,
          "metrics.refresh.enabled"                           -> "true",
          "metrics.refresh.interval"                          -> "5.minutes",
          "metrics.refresh.initialDelay"                      -> "5.seconds",
          "microservice.services.service-dependencies.port"   -> endpointPort,
          "microservice.services.service-dependencies.host"   -> host,
          "microservice.services.leak-detection.port"         -> endpointPort,
          "microservice.services.leak-detection.host"         -> host,
          "github.open.api.rawurl"                            -> endpointMockUrl,
          "github.open.api.token"                             -> "test-token",
          "jenkins.username"                                  -> "test-username",
          "jenkins.token"                                     -> "test-token",
          "microservice.services.teams-and-repositories.port" -> endpointPort,
          "microservice.services.teams-and-repositories.host" -> host,
          "github.rest.api.url"                               -> endpointMockUrl,
          "microservice.services.service-configs.port"        -> endpointPort,
          "microservice.services.service-configs.host"        -> host,
          "metrics.jvm"                                       -> false
        )
      )
      .build()

  "Service Health Indicators" should {
    "return 200 when it starts correctly and receives GET /ping/ping" in {
      val response = ws.url(s"http://localhost:$port/ping/ping").get.futureValue

      response.status shouldBe 200
    }

    "return correct json when HealthIndicatorController.indicator receives a get request with valid repo name" in {

      serviceEndpoint(GET, "/api/repositories", willRespondWith = (200, Some(teamsAndReposJson)))

      serviceEndpoint(GET, "/api/dependencies/auth", willRespondWith = (200, Some(serviceDependenciesJson)))

      serviceEndpoint(GET, url = "/api/leaks", queryParameters = Seq("repository" -> "auth"), willRespondWith = (200, Some(leakDetectionJson)))

      serviceEndpoint(GET, "/api/jenkins-url/auth", willRespondWith = (200, Some(teamsAndReposJenkinsJson)))

      serviceEndpoint(GET, "/alert-configs/auth", willRespondWith = (200, Some(serviceConfigsJson)))

      val jenkinsCred = s"Basic ${BaseEncoding.base64().encode("test-username:test-token".getBytes("UTF-8"))}"
      serviceEndpoint(
        GET,
        s"$endpointMockUrl/job/GG/job/auth/api/json.*",
        queryParameters = Seq("depth" -> "1", "tree" -> "lastCompletedBuild%5Bresult,timestamp%5D"),
        extraHeaders = Map("Authorization" -> jenkinsCred),
        willRespondWith = (404, None)
      )

      serviceEndpoint(
        GET,
        "/hmrc/auth/HEAD/README.md",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (404, None)
      )

      serviceEndpoint(
        GET,
        "/repos/hmrc/auth/pulls?state=open",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (200, Some("""[]""".stripMargin))
      )

      eventually {
        val response = ws.url(s"http://localhost:$port/health-indicators/indicators/auth").get.futureValue
        response.status shouldBe 200
        println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@")
        println(response.body)
        response.body     should include(expectedResponse)
        response.body     should include(bobbyRuleResponse)
        response.body     should include(leakDetectionResponse)
        response.body     should include(githubResponse)
        response.body     should include(buildStabilityResponse)
        response.body     should include(alertConfigResponse)
      }
    }
  }

  val leakDetectionJson =
    """
      [
         {
          "repoName": "auth",
          "branch": "main",
          "filePath": "/this/is/a/test",
          "scope": "fileName",
          "lineNumber": 1,
          "urlToSource": "https://test-url",
          "ruleId": "filename_test",
          "description": "test123",
          "lineText": "test.text"
         }
      ]

      """

  val serviceDependenciesJson =
    """{
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

  val teamsAndReposJenkinsJson =
    s"""
       |{
       | "jenkinsURL": "$endpointMockUrl/job/GG/job/auth/"
       |}
       |""".stripMargin

  val serviceConfigsJson =
    """
      |{
      |"serviceName": "auth",
      |"production": false
      |}
      |""".stripMargin

  val bobbyRuleResponse =
    """{"metricType":"bobby-rule","score":-20,"breakdown":[{"points":-20,"description":"simple-reactivemongo - TEST DEPRECATION"}]}"""
  val leakDetectionResponse =
    """{"metricType":"leak-detection","score":-15,"breakdown":[{"points":-15,"description":"Branch main has an unresolved filename_test leak"}]}"""
  val githubResponse =
    """{"metricType":"github","score":-10,"breakdown":[{"points":-10,"description":"No Readme defined"}]}"""
  val buildStabilityResponse =
    """{"metricType":"build-stability","score":0,"breakdown":[{"points":0,"description":"No Jenkins Build Found for: auth"}]}"""
  val alertConfigResponse =
    """{"metricType":"alert-config","score":20,"breakdown":[{"points":20,"description":"Alert Config is Disabled"}]}"""
  val expectedResponse = """"repoName":"auth","repoType":"Prototype","overallScore":-25,"""
}


