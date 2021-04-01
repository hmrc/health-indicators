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

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class JenkinsConnectorSpec
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
          "jenkins.username" -> "username",
          "jenkins.token"    -> "token"
        )
      )
      .build()

  val connector = app.injector.instanceOf[JenkinsConnector]

  "Get build job" should {
    "Return health report for build" in {

      serviceEndpoint(
        GET,
        "/job/GG/job/test/api/json?depth=1&tree=lastCompletedBuild%5Bresult,timestamp%5D",
        willRespondWith = (
          200,
          Some(
            """
              {
              |"_class": "hudson.model.FreeStyleProject",
              |"lastCompletedBuild": {
              |"_class": "hudson.model.FreeStyleBuild",
              |"result": "SUCCESS",
              |"timestamp": 1614779578869
              |}
              |}
              |""".stripMargin
          )
        )
      )
      val response = connector
        .getBuildJob(s"http://$host:$endpointPort/job/GG/job/test/")
        .futureValue
        .value

      val expectedOutput = JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.ofEpochMilli(1614779578869L))))

      response shouldBe expectedOutput
    }

    "Return health report for build that has never failed" in {

      serviceEndpoint(
        GET,
        "/job/GG/job/test/api/json?depth=1&tree=lastCompletedBuild%5Bresult,timestamp%5D",
        willRespondWith = (
          200,
          Some(
            """
              {
              |"_class": "hudson.model.FreeStyleProject",
              |"lastCompletedBuild": {
              |"_class": "hudson.model.FreeStyleBuild",
              |"result": "SUCCESS",
              |"timestamp": 1614779578869
              |}
              |}
              |""".stripMargin
          )
        )
      )
      val response = connector
        .getBuildJob(s"http://$host:$endpointPort/job/GG/job/test/")
        .futureValue
        .value

      val expectedOutput = JenkinsBuildReport(Some(JenkinsBuildStatus("SUCCESS", Instant.ofEpochMilli(1614779578869L))))

      response shouldBe expectedOutput
    }

    "Return a None if build job not found" in {
      serviceEndpoint(
        GET,
        "/job/GG/job/test/api/json?depth=1&tree=lastCompletedBuild%5Bresult,timestamp%5D",
        willRespondWith = (404, None)
      )

      val response: Option[JenkinsBuildReport] = connector
        .getBuildJob(s"http://$host:$endpointPort/job/GG/job/non-existing/")
        .futureValue

      response shouldBe None
    }
  }

}
