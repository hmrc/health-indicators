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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.healthindicators.WireMockEndpoints
import uk.gov.hmrc.healthindicators.connectors.RepositoryType.{Library, Other, Prototype, Service}
import uk.gov.hmrc.http.HeaderCarrier

class TeamsAndRepositoriesConnectorSpec
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
          "microservice.services.teams-and-repositories.port" -> endpointPort,
          "microservice.services.teams-and-repositories.host" -> host,
          "metrics.jvm"                                       -> false
        )
      )
      .build()

  private lazy val teamsAndRepositoriesConnector = app.injector.instanceOf[TeamsAndRepositoriesConnector]

  "GET jenkinsUrl" should {
    "use repository name to return a jenkins url" in new Setup {
      serviceEndpoint(
        GET,
        "/api/jenkins-url/test",
        willRespondWith = (
          200,
          Some(
            """
              |{
              | "jenkinsURL": "https://build.tax.service.gov.uk/job/GG/job/test"
              |}
              |""".stripMargin
          )
        )
      )

      val response = teamsAndRepositoriesConnector
        .getJenkinsUrl("test")
        .futureValue

      val expectedResult = Some(JenkinsUrl("https://build.tax.service.gov.uk/job/GG/job/test"))

      response shouldBe expectedResult
    }

    "use repository name to return a None when no jenkins url found" in new Setup {
      serviceEndpoint(GET, "/api/jenkins-url/test", willRespondWith = (404, None))
      val response = teamsAndRepositoriesConnector
        .getJenkinsUrl("test")
        .futureValue

      response shouldBe None
    }
  }

  "GET allRepositories" should {
    "return a list of all repositories" in new Setup {
      serviceEndpoint(
        GET,
        "/api/repositories",
        willRespondWith = (
          200,
          Some(
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
        TeamsAndRepos("2-way-messaging-prototype", Prototype),
        TeamsAndRepos("2way-testbed-prototype", Prototype),
        TeamsAndRepos("5mld-prototype", Prototype)
      )

      response shouldBe expectedResult
    }

    "bind query string correctly when given a valid repoType" in {
      val paramsPrototype = Map("repoType" -> Seq(Prototype.toString))
      val paramsService   = Map("repoType" -> Seq(Service.toString))
      val paramsLibrary   = Map("repoType" -> Seq(Library.toString))
      val paramsOther     = Map("repoType" -> Seq(Other.toString))

      RepositoryType.queryStringBindable.bind(key = "repoType", paramsPrototype).value shouldBe Right(Prototype)
      RepositoryType.queryStringBindable.bind(key = "repoType", paramsService).value   shouldBe Right(Service)
      RepositoryType.queryStringBindable.bind(key = "repoType", paramsLibrary).value   shouldBe Right(Library)
      RepositoryType.queryStringBindable.bind(key = "repoType", paramsOther).value     shouldBe Right(Other)
    }

    "not bind when no query string is given" in {
      RepositoryType.queryStringBindable.bind(key = "repoType", Map.empty) shouldBe None
    }

    "fail to bind when there is no matching value" in {
      val params = Map("sort" -> Seq.empty)
      RepositoryType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }

    "fail to bind when value is not recognised" in {
      val params = Map("sort" -> Seq("unknown"))
      RepositoryType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }

    "fail to bind when more than one value is given" in {
      val params = Map("sort" -> Seq(Prototype.toString, Library.toString))
      RepositoryType.queryStringBindable.bind(key = "repoType", params) shouldBe None
    }
  }

  private trait Setup {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  }

}
