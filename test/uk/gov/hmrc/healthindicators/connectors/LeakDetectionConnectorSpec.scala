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
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class LeakDetectionConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with HttpClientV2Support
     with WireMockSupport {

  private lazy val leakDetectionConnector =
    new LeakDetectionConnector(
      httpClientV2,
      new ServicesConfig(Configuration(
        "microservice.services.leak-detection.port" -> wireMockPort,
        "microservice.services.leak-detection.host" -> wireMockHost
      ))
    )

  "GET latestMasterReport" should {
    "return a list of leak detections reports for a repository" in {
      stubFor(
        get(urlEqualTo("/api/leaks?repository=repo1"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                """[
                  {
                    "repoName": "repo1",
                    "branch": "main",
                    "timestamp": "2019-04-01T10:26:57.486Z",
                    "reportId": "a5c4a789-697b-4964-90cf-a7fbd77f377b",
                    "ruleId": "rule1",
                    "description": "lds rule 1",
                    "filePath": "/keys",
                    "scope": "fileName",
                    "lineNumber": 1,
                    "urlToSource": "https://github.com/",
                    "lineText": "keystore",
                    "matches": [
                      {
                        "start": 8,
                        "end": 12
                      }
                    ],
                    "priority": "low"
                  },
                  {
                    "repoName": "repo1",
                    "branch": "branch1",
                    "timestamp": "2019-04-01T10:26:57.486Z",
                    "reportId": "a5c4a789-697b-4964-90cf-a7fbd77f377b",
                    "ruleId": "rule2",
                    "description": "lds rule 2",
                    "filePath": "/keys",
                    "scope": "fileName",
                    "lineNumber": 1,
                    "urlToSource": "https://github.com/",
                    "lineText": "text",
                    "matches": [
                      {
                        "start": 6,
                        "end": 10
                      }
                    ],
                    "priority": "low"
                  }
                ]"""
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
      stubFor(
        get(urlEqualTo("/api/leaks?repository=non-existing"))
          .willReturn(
            aResponse()
            .withStatus(200)
            .withBody("[]")
          )
      )

      val response = leakDetectionConnector
        .findLeaks("non-existing")
        .futureValue

      response shouldBe Nil
    }
  }
}
