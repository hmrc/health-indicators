/*
 * Copyright 2024 HM Revenue & Customs
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
      .configure(
        Map(
          "mongodb.uri"                                       -> mongoUri,
          "metrics.refresh.enabled"                           -> "true",
          "metrics.refresh.interval"                          -> "5.minutes",
          "metrics.refresh.initialDelay"                      -> "5.seconds",
          "microservice.services.leak-detection.port"         -> wireMockPort,
          "microservice.services.leak-detection.host"         -> wireMockHost,
          "microservice.services.platops-github-proxy.port"   -> wireMockPort,
          "microservice.services.platops-github-proxy.host"   -> wireMockHost,
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
        get(urlEqualTo("/api/leaks?repository=auth"))
          .willReturn(aResponse().withStatus(200).withBody(leakDetectionJson))
      )

      stubFor(
        get(urlEqualTo("/service-configs/alert-configs/auth"))
          .willReturn(aResponse().withStatus(200).withBody(serviceConfigsJson))
      )

      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-raw/auth/HEAD/README.md"))
          .willReturn(aResponse().withStatus(404))
      )

      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-rest/auth/pulls?state=open"))
          .willReturn(aResponse().withStatus(200).withBody("""[]"""))
      )

      eventually {
        val response = ws.url(s"http://localhost:$port/health-indicators/indicators/auth").get().futureValue
        response.status shouldBe 200
        response.body     should include(expectedResponse)
        response.body     should include(leakDetectionResponse)
        response.body     should include(githubResponse)
        response.body     should include(alertConfigResponse)
      }

      verify(
        getRequestedFor(urlEqualTo("/platops-github-proxy/github-raw/auth/HEAD/README.md"))
      )

      verify(
        getRequestedFor(urlEqualTo("/platops-github-proxy/github-rest/auth/pulls?state=open"))
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

  val serviceConfigsJson =
    """
      {
      "serviceName": "auth",
      "production": false
      }
    """

  val leakDetectionResponse =
    """{"metricType":"leak-detection","score":-15,"breakdown":[{"points":-15,"description":"Branch main has an unresolved filename_test leak"}]}"""
  val githubResponse =
    """{"metricType":"github","score":-10,"breakdown":[{"points":-10,"description":"No Readme defined"}]}"""
  val alertConfigResponse =
    """{"metricType":"alert-config","score":20,"breakdown":[{"points":20,"description":"Alert Config is Disabled"}]}"""
  val expectedResponse = """"repoName":"auth","repoType":"Prototype","overallScore":-5,"""
}
