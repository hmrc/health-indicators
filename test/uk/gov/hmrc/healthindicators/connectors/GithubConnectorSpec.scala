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

package uk.gov.hmrc.healthindicators.connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Configuration
import uk.gov.hmrc.healthindicators.configs.GithubConfig
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class GithubConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with HttpClientV2Support
     with WireMockSupport {

  private lazy val githubConnector =
    new GithubConnector(
      httpClientV2,
      new GithubConfig(Configuration(
        "github.open.api.rawurl" -> wireMockUrl,
        "github.rest.api.url"    -> wireMockUrl,
        "github.open.api.token"  -> "test-token"
      ))
    )

  "GET findReadMe" should {
    "return a URL of README.md for the correct repo" in {

      stubFor(
        get(urlEqualTo("/hmrc/repo1/HEAD/README.md"))
          .willReturn(aResponse().withStatus(200).withBody("Hello World"))
      )

      val response = githubConnector
        .findReadMe("repo1")
        .futureValue
        .value

      response shouldBe "Hello World"

      verify(
        getRequestedFor(urlEqualTo("/hmrc/repo1/HEAD/README.md"))
          .withHeader("Authorization", equalTo("token test-token"))
      )
    }

    "return a None when no README.md is found" in {
      stubFor(
        get(urlEqualTo("/hmrc/repo1/HEAD/README.md"))
          .willReturn(aResponse().withStatus(404))
      )

      val response = githubConnector
        .findReadMe("repo1")
        .futureValue

      response shouldBe None
    }
  }

  "getOpenPrs" should {
    "respond with correct PR data" in {
      stubFor(
        get(urlEqualTo("/repos/hmrc/repo2/pulls?state=open"))
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

      val response = githubConnector
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
        getRequestedFor(urlPathEqualTo("/repos/hmrc/repo2/pulls"))
          .withHeader("Authorization", equalTo("token test-token"))
      )

    }

    "respond with None when repo not found" in {
      stubFor(
        get(urlEqualTo("/repos/hmrc/repo2/pulls?state=open"))
          .willReturn(aResponse().withStatus(404))
      )

      val response = githubConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe None
    }

    "respond with Seq.empty when no pulls found" in {
      stubFor(
        get(urlEqualTo("/repos/hmrc/repo2/pulls?state=open"))
          .willReturn(aResponse().withStatus(200).withBody("[]"))
      )

      val response = githubConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe Some(Seq.empty)
    }
  }
}
