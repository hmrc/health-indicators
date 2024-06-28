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

import scala.concurrent.ExecutionContext.Implicits.global

class TeamsAndRepositoriesConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with WireMockSupport
     with HttpClientV2Support {

  private val config =
    Configuration(
      "microservice.services.teams-and-repositories.port" -> wireMockPort,
      "microservice.services.teams-and-repositories.host" -> wireMockHost
    )

  val teamsAndRepositoriesConnector =
    new TeamsAndRepositoriesConnector(httpClientV2, new ServicesConfig(config))

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "GET allRepositories" should {
    "return a list of all repositories" in {
      stubFor(
        get(urlEqualTo("/api/repositories"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                """[{
                  "name": "2-way-messaging-prototype",
                  "createdAt": 1541588042000,
                  "lastUpdatedAt": 1601630778000,
                  "repoType": "Prototype",
                  "language": "HTML",
                  "archived": false
                },
                {
                  "name": "2way-testbed-prototype",
                  "createdAt": 1570720430000,
                  "lastUpdatedAt": 1605862299000,
                  "repoType": "Prototype",
                  "language": "HTML",
                  "archived": false
                },
                {
                  "name": "5mld-prototype",
                  "createdAt": 1576747271000,
                  "lastUpdatedAt": 1581517326000,
                  "repoType": "Prototype",
                  "language": "CSS",
                  "archived": false
                }]"""
              )
          )
      )

      val response = teamsAndRepositoriesConnector
        .allRepositories(headerCarrier)
        .futureValue

      val expectedResult = List(
        TeamsAndRepos("2-way-messaging-prototype", RepoType.Prototype),
        TeamsAndRepos("2way-testbed-prototype"   , RepoType.Prototype),
        TeamsAndRepos("5mld-prototype"           , RepoType.Prototype)
      )

      response shouldBe expectedResult
    }

    "bind query string correctly when given a valid repoType" in {
      val paramsPrototype = Map("repoType" -> Seq(RepoType.Prototype.toString))
      val paramsService   = Map("repoType" -> Seq(RepoType.Service.toString))
      val paramsLibrary   = Map("repoType" -> Seq(RepoType.Library.toString))
      val paramsOther     = Map("repoType" -> Seq(RepoType.Other.toString))

      RepoType.queryStringBindable.bind(key = "repoType", paramsPrototype).value shouldBe Right(RepoType.Prototype)
      RepoType.queryStringBindable.bind(key = "repoType", paramsService  ).value shouldBe Right(RepoType.Service)
      RepoType.queryStringBindable.bind(key = "repoType", paramsLibrary  ).value shouldBe Right(RepoType.Library)
      RepoType.queryStringBindable.bind(key = "repoType", paramsOther    ).value shouldBe Right(RepoType.Other)
    }

    "not bind when no query string is given" in {
      RepoType.queryStringBindable.bind(key = "repoType", Map.empty) shouldBe None
    }

    "fail to bind when there is no matching value" in {
      val params = Map("sort" -> Seq.empty)
      RepoType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }

    "fail to bind when value is not recognised" in {
      val params = Map("sort" -> Seq("unknown"))
      RepoType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }

    "fail to bind when more than one value is given" in {
      val params = Map("sort" -> Seq(RepoType.Prototype.toString, RepoType.Library.toString))
      RepoType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }
  }
}
