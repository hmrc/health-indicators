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

package uk.gov.hmrc.healthindicators.connectors

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.healthindicators.WireMockEndpoints
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GithubConnectorSpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues
    with WireMockEndpoints {

  override def fakeApplication: Application =
    new GuiceApplicationBuilder()
      .disable(classOf[com.kenshoo.play.metrics.PlayModule])
      .configure(
        Map(
          "github.open.api.rawurl" -> endpointMockUrl,
          "github.rest.api.url" -> endpointMockUrl,
          "github.open.api.token"  -> "test-token",
          "metrics.jvm"            -> false
        )
      )
      .build()

  private lazy val githubConnector = app.injector.instanceOf[GithubConnector]

  "GET findReadMe" should {
    "return a URL of README.md for the correct repo" in {

      serviceEndpoint(
        GET,
        "/hmrc/repo1/master/README.md",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (200, Some("Hello World"))
      )

      val response = githubConnector
        .findReadMe("repo1")
        .futureValue
        .value

      response shouldBe "Hello World"
    }

    "return a None when no README.md is found" in {

      serviceEndpoint(
        GET,
        "/hmrc/repo1/master/README.md",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (404, None)
      )

      val response = githubConnector
        .findReadMe("repo1")
        .futureValue

      response shouldBe None
    }
  }

  "getOpenPrs" should {
    "respond with correct PR data" in {
      val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
      val testJSON = Some(
        """[{
          |"title": "hello-world",
          |"created_at": "2021-04-16T13:38:36Z",
          |"updated_at": "2021-04-16T13:38:33Z"
          |}]""".stripMargin)

      serviceEndpoint(
        GET,
        "/repos/hmrc/repo2/pulls",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (200, testJSON)
      )

      val response = githubConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe Some(Seq(OpenPR("hello-world",
        LocalDate.parse("2021-04-16T13:38:36Z", dateFormatter),
        LocalDate.parse("2021-04-16T13:38:33Z", dateFormatter))))
    }

    "respond with None when repo not found" in {
      serviceEndpoint(
        GET,
        "/repos/hmrc/repo2/pulls",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (404, None)
      )

      val response = githubConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe None
    }

    "respond with Seq.empty when no pulls found" in {
      serviceEndpoint(
        GET,
        "/repos/hmrc/repo2/pulls",
        requestHeaders = Map("Authorization" -> s"token test-token"),
        willRespondWith = (200,
          Some("""[]""".stripMargin))
      )

      val response = githubConnector
        .getOpenPRs("repo2")
        .futureValue

      response shouldBe Some(Seq.empty)
    }
  }

}
