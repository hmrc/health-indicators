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

class ServiceConfigsConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with HttpClientV2Support
     with WireMockSupport {

  private lazy val serviceConfigsConnector =
    new ServiceConfigsConnector(
      httpClientV2,
      new ServicesConfig(Configuration(
        "microservice.services.service-configs.port" -> wireMockPort,
        "microservice.services.service-configs.host" -> wireMockHost
      ))
    )

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  "GET findAlertConfigs" should {
    "return AlertConfig for repo" in {
      stubFor(
        get(urlEqualTo("/service-configs/alert-configs/foo"))
          .willReturn(
            aResponse()
            .withStatus(200)
            .withBody(
              """{
                "serviceName": "foo",
                "production": true
              }"""
            )
          )
      )

      val response = serviceConfigsConnector
        .findAlertConfigs("foo")
        .futureValue
        .value

      val expectedOutput = AlertConfig(true)

      response shouldBe expectedOutput
    }

    "return AlertConfig for repo when config is not found" in {
      stubFor(
        get(urlEqualTo("/service-configs/alert-configs/foo"))
          .willReturn(aResponse().withStatus(404))
      )

      val response: Option[AlertConfig] = serviceConfigsConnector
        .findAlertConfigs("foo")
        .futureValue

      response shouldBe None
    }
  }
}
