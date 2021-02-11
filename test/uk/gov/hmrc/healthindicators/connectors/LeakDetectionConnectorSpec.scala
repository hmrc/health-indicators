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
import play.api.libs.json.{JsSuccess, Json, Reads}
import uk.gov.hmrc.healthindicators.WireMockEndpoints
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.http.HeaderCarrier

class LeakDetectionConnectorSpec
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
          "microservice.services.leak-detection.port" -> endpointPort,
          "microservice.services.leak-detection.host" -> host,
          "metrics.jvm" -> false
        ))
      .build()

  private lazy val serviceDependenciesConnector = app.injector.instanceOf[LeakDetectionConnector]

  "GET Reports" should {
    "return a list of leak detections reports for a repository" in new Setup {
      serviceEndpoint(
        GET,
        "/api/reports/repositories/repo1",
        willRespondWith = (
          200,
          Some(
            """
            |  {
            |   "_id": "123",
            |   "inspectionResults": [
            |     {
            |      "filePath": "/s3/src/test/resources/keystore.jks",
            |      "scope": "fileName",
            |      "lineNumber": 1,
            |      "urlToSource": "https://test-url",
            |      "ruleId": "filename_private_key_11",
            |      "description": "test123",
            |      "lineText": "keystore.jks"
            |     }
            |   ]
              }""".stripMargin
          ))
      )

      val response = serviceDependenciesConnector
        .findLatestMasterReport("repo1")
        .futureValue
        .value

      val expectedOutput = Report("123",
        Seq(ReportLine
        (
          "/s3/src/test/resources/keystore.jks",
          "fileName",
          1,
          "https://test-url",
          Some("filename_private_key_11"),
          "test123",
          "keystore.jks"
        )
        ))
      response shouldBe expectedOutput
    }

    "ReportLine" should {
      implicit val rlR: Reads[ReportLine] = ReportLine.reads
      "parse json" in {
        val jsonInput =
          """{"filePath": "/s3/src/test/resources/keystore.jks",
          "scope": "fileName",
          "lineNumber": 1,
          "urlToSource": "https://github.com/hmrc/alpakka/blame/96c3cdf1542b96a82bee10c8a2339f282b7230a0/s3/src/test/resources/keystore.jks#L1",
          "ruleId": "filename_private_key_11",
          "description": "Extension indicates Java KeyStore file, often containing private keys",
          "lineText": "keystore.jks"}"""
        val objectOutput = Json.parse(jsonInput).validate[ReportLine]
        objectOutput shouldBe
          JsSuccess(ReportLine(
            "/s3/src/test/resources/keystore.jks",
            "fileName",
            1,
            "https://github.com/hmrc/alpakka/blame/96c3cdf1542b96a82bee10c8a2339f282b7230a0/s3/src/test/resources/keystore.jks#L1",
            Some("filename_private_key_11"),
            "Extension indicates Java KeyStore file, often containing private keys",
            "keystore.jks"
          ))
      }
    }
  }

  private trait Setup {
    implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  }

}
