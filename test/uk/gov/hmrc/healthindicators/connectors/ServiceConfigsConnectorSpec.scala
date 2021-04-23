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

class ServiceConfigsConnectorSpec
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
          "microservice.services.service-configs.port" -> endpointPort,
          "microservice.services.service-configs.host" -> host,
          "metrics.jvm"                               -> false
        )
      )
      .build()

  private lazy val serviceConfigsConnector = app.injector.instanceOf[ServiceConfigsConnector]

  "GET findAlertConfigs" should {
    "Return AlertConfig for repo that has config enabled" in {

      serviceEndpoint(
        GET,
        "/alert-configs/foo",
        willRespondWith = (200,
          Some(
            """
              |{
              |"serviceName": "foo",
              |"production": true
              |}
              |""".stripMargin
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

    "Return AlertConfig for repo that has config disabled" in {

      serviceEndpoint(
        GET,
        "/alert-configs/foo",
        willRespondWith = (200,
          Some(
            """
              |{
              |"serviceName": "foo",
              |"production": false
              |}
              |""".stripMargin
          )
        )
      )

      val response = serviceConfigsConnector
        .findAlertConfigs("foo")
        .futureValue
        .value

      val expectedOutput = AlertConfig(false)

      response shouldBe expectedOutput
    }

    "Return AlertConfig for repo when config is not found" in {

      serviceEndpoint(
        GET,
        "/alert-configs/foo",
        willRespondWith = (404, None)
      )

      val response: Option[AlertConfig] = serviceConfigsConnector
        .findAlertConfigs("foo")
        .futureValue

      response shouldBe None
    }
  }

}
