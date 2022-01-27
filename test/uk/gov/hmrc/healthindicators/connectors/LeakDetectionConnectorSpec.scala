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

import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
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
          "metrics.jvm"                               -> false
        )
      )
      .build()

  private lazy val leakDetectionConnector = app.injector.instanceOf[LeakDetectionConnector]

  "GET latestMasterReport" should {
    "return a list of leak detections reports for a repository" in {
      serviceEndpoint(
        GET,
        "/api/leaks",
        queryParameters = Seq("repository" -> "repo1"),
        willRespondWith = (
          200,
          Some(
            """
            |[ {
            |    "repoName": "repo1",
            |    "branch": "main",
            |    "timestamp": "2019-04-01T10:26:57.486Z",
            |    "reportId": "a5c4a789-697b-4964-90cf-a7fbd77f377b",
            |    "ruleId": "rule1",
            |    "description": "lds rule 1",
            |    "filePath": "/keys",
            |    "scope": "fileName",
            |    "lineNumber": 1,
            |    "urlToSource": "https://github.com/",
            |    "lineText": "keystore",
            |    "matches": [
            |      {
            |        "start": 8,
            |        "end": 12
            |      }
            |    ],
            |    "priority": "low"
            |  },
            |  {
            |    "repoName": "repo1",
            |    "branch": "branch1",
            |    "timestamp": "2019-04-01T10:26:57.486Z",
            |    "reportId": "a5c4a789-697b-4964-90cf-a7fbd77f377b",
            |    "ruleId": "rule2",
            |    "description": "lds rule 2",
            |    "filePath": "/keys",
            |    "scope": "fileName",
            |    "lineNumber": 1,
            |    "urlToSource": "https://github.com/",
            |    "lineText": "text",
            |    "matches": [
            |      {
            |        "start": 6,
            |        "end": 10
            |      }
            |    ],
            |    "priority": "low"
            |  }
            |]""".stripMargin
          )
        )
      )

      val response = leakDetectionConnector
        .findLeaks("repo1")
        .futureValue

      val expectedOutput = Seq(Leak("repo1", "main", "rule1"), Leak("repo1", "branch1", "rule2"))
      response shouldBe expectedOutput
    }

    "return a Empty List for non existing repository" in {
      serviceEndpoint(GET, "/api/leaks", queryParameters = Seq("repository" -> "non-existing"),  willRespondWith = (200, Some("[]")))

      val response = leakDetectionConnector
        .findLeaks("non-existing")
        .futureValue

      response shouldBe Nil
    }

  }

}
