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
import play.api.libs.json.{JsSuccess, Json, Reads}
import uk.gov.hmrc.healthindicators.WireMockEndpoints
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ServiceDependenciesConnectorSpec
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
          "microservice.services.service-dependencies.port" -> endpointPort,
          "microservice.services.service-dependencies.host" -> host,
          "metrics.jvm"                                     -> false
        )
      )
      .build()

  private lazy val serviceDependenciesConnector = app.injector.instanceOf[ServiceDependenciesConnector]

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  "GET dependencies" should {
    "return a list of leak detections reports for a repository that exists" in new Setup {
      val testJson = """{
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

      serviceEndpoint(
        GET,
        "/api/dependencies/repo1",
        willRespondWith = (200, Some(testJson))
      )
      val response: Dependencies = serviceDependenciesConnector
        .dependencies("repo1")
        .futureValue
        .value

      val expectedResponse: Dependencies = Dependencies(
        "auth",
        Seq(
          Dependency(
            Seq(BobbyRuleViolation("TEST DEPRECATION", LocalDate.parse("2050-05-01"), "(,99.99.99)")),
            "simple-reactivemongo"
          )
        ),
        Seq(),
        Seq()
      )

      implicit val df: Reads[Dependencies] = Dependencies.reads
      val objectOutput                     = Json.parse(testJson).validate[Dependencies]
      println(objectOutput.toString)

      response shouldBe expectedResponse
    }
    "return None when a repo does not exist" in new Setup {
      serviceEndpoint(
        GET,
        "/api/dependencies/repo1",
        willRespondWith = (
          404,
          None
        )
      )

      val response = serviceDependenciesConnector
        .dependencies("repo1")
        .futureValue

      response shouldBe None
    }
  }

  "BobbyRuleViolation" should {
    implicit val brvR: Reads[BobbyRuleViolation] = BobbyRuleViolation.reads
    "parse json correctly" in {
      val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val jsonInput                        = """{"reason": "reason", "from": "1999-01-01", "range": "range"}"""
      val objectOutput                     = Json.parse(jsonInput).validate[BobbyRuleViolation]
      objectOutput shouldBe
        JsSuccess(BobbyRuleViolation("reason", LocalDate.parse("1999-01-01", dateFormatter), "range"))
    }
  }

}

private trait Setup {
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
}
