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
import uk.gov.hmrc.healthindicators.configs.JenkinsConfig
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class JenkinsConnectorSpec
  extends AnyWordSpec
     with Matchers
     with OptionValues
     with ScalaFutures
     with IntegrationPatience
     with HttpClientV2Support
     with WireMockSupport {

  val connector =
    new JenkinsConnector(
      new JenkinsConfig(Configuration(
        "jenkins.username" -> "username",
        "jenkins.token"    -> "token",
        "jenkins.host"     -> wireMockUrl
      )),
      httpClientV2
    )

  "Get build job" should {
    "return health report for build" in {
      stubFor(
        get(urlEqualTo("/job/GG/job/test/api/json?depth=1&tree=lastCompletedBuild%5Bresult,timestamp%5D"))
          .willReturn(
            aResponse()
              .withStatus(200)
              .withBody(
                """{
                  "_class": "hudson.model.FreeStyleProject",
                  "lastCompletedBuild": {
                    "_class": "hudson.model.FreeStyleBuild",
                    "result": "SUCCESS",
                    "timestamp": 1614779578869
                  }
                }"""
              )
          )
      )

      val response = connector
        .getBuildJob(s"$wireMockUrl/job/GG/job/test/")
        .futureValue
        .value

      val expectedOutput = JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.ofEpochMilli(1614779578869L))))

      response shouldBe expectedOutput
    }

    "return a None if build job not found" in {
      stubFor(
        get(urlEqualTo("/job/GG/job/test/api/json?depth=1&tree=lastCompletedBuild%5Bresult,timestamp%5D"))
          .willReturn(aResponse().withStatus(404))
      )

      val response: Option[JenkinsBuildReport] = connector
        .getBuildJob(s"$wireMockUrl/job/GG/job/non-existing/")
        .futureValue

      response shouldBe None
    }
  }
}
