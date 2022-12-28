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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.google.common.io.BaseEncoding
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import play.api.Application
import uk.gov.hmrc.healthindicators.models.RepositoryMetrics
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class IntegrationSpec
  extends AnyWordSpec
     with DefaultPlayMongoRepositorySupport[RepositoryMetrics]
     with GuiceOneServerPerSuite
     with Matchers
     with ScalaFutures
     with IntegrationPatience
     with WireMockSupport
     with Eventually {

  protected val repository = app.injector.instanceOf[RepositoryMetricsRepository]

  private[this] lazy val ws = app.injector.instanceOf[WSClient]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable(classOf[com.kenshoo.play.metrics.PlayModule])
      .configure(
        Map(
          "mongodb.uri"                                       -> mongoUri,
          "metrics.refresh.enabled"                           -> "true",
          "metrics.refresh.interval"                          -> "5.minutes",
          "metrics.refresh.initialDelay"                      -> "5.seconds",
          "microservice.services.service-dependencies.port"   -> wireMockPort,
          "microservice.services.service-dependencies.host"   -> wireMockHost,
          "microservice.services.leak-detection.port"         -> wireMockPort,
          "microservice.services.leak-detection.host"         -> wireMockHost,
          "github.open.api.rawurl"                            -> wireMockUrl,
          "github.open.api.token"                             -> "test-token",
          "jenkins.username"                                  -> "test-username",
          "jenkins.token"                                     -> "test-token",
          "microservice.services.teams-and-repositories.port" -> wireMockPort,
          "microservice.services.teams-and-repositories.host" -> wireMockHost,
          "github.rest.api.url"                               -> wireMockUrl,
          "microservice.services.service-configs.port"        -> wireMockPort,
          "microservice.services.service-configs.host"        -> wireMockHost,
          "metrics.jvm"                                       -> false
        )
      )
      .build()

  "Service Health Indicators" should {
    "return 200 when it starts correctly and receives GET /ping/ping" in {
      val response = ws.url(s"http://localhost:$port/ping/ping").get().futureValue

      response.status shouldBe 200
    }

    "return correct json when HealthIndicatorController.indicator receives a get request with valid repo name" in {
      stubFor(
        get(urlEqualTo("/api/repositories"))
          .willReturn(aResponse().withStatus(200).withBody(teamsAndReposJson))
      )

      stubFor(
        get(urlEqualTo("/api/dependencies/auth"))
          .willReturn(aResponse().withStatus(200).withBody(serviceDependenciesJson))
      )

      stubFor(
        get(urlEqualTo("/api/leaks?repository=auth"))
          .willReturn(aResponse().withStatus(200).withBody(leakDetectionJson))
      )

      stubFor(
        get(urlEqualTo("/api/jenkins-url/auth"))
          .willReturn(aResponse().withStatus(200).withBody(teamsAndReposJenkinsJson))
      )

      stubFor(
        get(urlEqualTo("/service-configs/alert-configs/auth"))
          .willReturn(aResponse().withStatus(200).withBody(serviceConfigsJson))
      )

      val jenkinsCred = s"Basic ${BaseEncoding.base64().encode("test-username:test-token".getBytes("UTF-8"))}"

      stubFor(
        get(urlPathEqualTo("/job/GG/job/auth/api/json"))
          .willReturn(aResponse().withStatus(404))
      )

      stubFor(
        get(urlEqualTo("/hmrc/auth/HEAD/README.md"))
          .willReturn(aResponse().withStatus(404))
      )

      stubFor(
        get(urlEqualTo("/repos/hmrc/auth/pulls?state=open"))
          .willReturn(aResponse().withStatus(200).withBody("""[]"""))
      )

      eventually {
        val response = ws.url(s"http://localhost:$port/health-indicators/indicators/auth").get().futureValue
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

      verify(
        getRequestedFor(urlPathEqualTo("/job/GG/job/auth/api/json"))
          .withQueryParam("depth", equalTo("1"))
          .withQueryParam("tree" , equalTo("lastCompletedBuild[result,timestamp]"))
          .withHeader("Authorization", equalTo(jenkinsCred))
      )

      verify(
        getRequestedFor(urlEqualTo("/hmrc/auth/HEAD/README.md"))
          .withHeader("Authorization", equalTo("token test-token"))
      )

      verify(
        getRequestedFor(urlEqualTo("/repos/hmrc/auth/pulls?state=open"))
          .withHeader("Authorization", equalTo("token test-token"))
      )
    }
  }

  val leakDetectionJson =
    """[
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
    ]"""

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
       {
        "jenkinsURL": "$wireMockUrl/job/GG/job/auth/"
       }
     """

  val serviceConfigsJson =
    """
      {
      "serviceName": "auth",
      "production": false
      }
    """

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
