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

package uk.gov.hmrc.healthindicators.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GitHubProxyConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with HttpClientV2Support
     with WireMockSupport {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val gitHubProxyConnector =
    new GitHubProxyConnector(
      httpClientV2,
      new ServicesConfig(Configuration(
        "microservice.services.platops-github-proxy.port" -> wireMockPort,
        "microservice.services.platops-github-proxy.host" -> wireMockHost
      ))
    )

  "GET findReadMe with github-raw" should {
    "return a URL of README.md for the correct repo" in {

      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-raw/repo1/HEAD/README.md"))
          .willReturn(aResponse().withStatus(200).withBody("Hello World"))
      )

      val response = gitHubProxyConnector
        .findReadMe("repo1")
        .futureValue
        .value

      response shouldBe "Hello World"

      verify(
        getRequestedFor(urlEqualTo("/platops-github-proxy/github-raw/repo1/HEAD/README.md"))
      )
    }

    "return a None when no README.md is found" in {
      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-raw/repo1/HEAD/README.md"))
          .willReturn(aResponse().withStatus(404))
      )

      val response = gitHubProxyConnector
        .findReadMe("repo1")
        .futureValue

      response shouldBe None
    }
  }

  "getOpenPrs" should {
    "respond with correct PR data" in {
      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-rest/repo2/pulls?state=open"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                """[
                  {
                    "title": "hello-world",
                    "created_at": "2021-04-16T13:38:36Z",
                    "updated_at": "2021-04-16T13:38:33Z"
                  }
                ]"""
              )
          )
      )

      val response = gitHubProxyConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe Some(
        Seq(
          OpenPR(
            "hello-world",
            LocalDate.parse("2021-04-16"),
            LocalDate.parse("2021-04-16")
          )
        )
      )

      verify(
        getRequestedFor(urlPathEqualTo("/platops-github-proxy/github-rest/repo2/pulls"))
      )
    }

    "respond with None when repo not found" in {
      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-rest/repo2/pulls?state=open"))
          .willReturn(aResponse().withStatus(404))
      )

      val response = gitHubProxyConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe None
    }

    "respond with Seq.empty when no pulls found" in {
      stubFor(
        get(urlEqualTo("/platops-github-proxy/github-rest/repo2/pulls?state=open"))
          .willReturn(aResponse().withStatus(200).withBody("[]"))
      )

      val response = gitHubProxyConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe Some(Seq.empty)
    }
  }
}
