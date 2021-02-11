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

  private lazy val leakDetectionConnector = app.injector.instanceOf[LeakDetectionConnector]

  "GET latestMasterReport" should {
    "return a list of leak detections reports for a repository" in {
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
            |      "filePath": "/this/is/a/test",
            |      "scope": "fileName",
            |      "lineNumber": 1,
            |      "urlToSource": "https://test-url",
            |      "ruleId": "filename_test",
            |      "description": "test123",
            |      "lineText": "test.text"
            |     }
            |   ]
              }""".stripMargin
          ))
      )

      val response = leakDetectionConnector
        .findLatestMasterReport("repo1")
        .futureValue
        .value

      val expectedOutput = Report("123",
        Seq(ReportLine(
          "/this/is/a/test",
          "fileName",
           1,
          "https://test-url",
          Some("filename_test"),
          "test123",
          "test.text"
        )
        ))
      response shouldBe expectedOutput
    }

    "return a None for non existing repository" in {
      serviceEndpoint(GET, "/api/reports/repositories/non-existing", willRespondWith = (404, None))

      val response = leakDetectionConnector
        .findLatestMasterReport("non-existing")
        .futureValue

      response shouldBe None
    }


    "ReportLine" should {
      implicit val rlR: Reads[ReportLine] = ReportLine.reads
      "parse json" in {
        val jsonInput =
          """
        |{
        |  "filePath": "/this/is/a/test",
        |  "scope": "fileName",
        |  "lineNumber": 1,
        |  "urlToSource": "https://test-url",
        |  "ruleId": "filename_test",
        |  "description": "test123",
        |  "lineText": "test.text"
        |}
        |""".stripMargin

        val objectOutput = Json.parse(jsonInput).validate[ReportLine]
        objectOutput shouldBe
          JsSuccess(ReportLine(
            "/this/is/a/test",
            "fileName",
             1,
            "https://test-url",
            Some("filename_test"),
            "test123",
            "test.text"
          ))
      }
    }
  }


}
