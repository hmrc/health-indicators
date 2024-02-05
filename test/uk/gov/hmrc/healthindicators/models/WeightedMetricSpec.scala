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

package uk.gov.hmrc.healthindicators.models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Writes, _}
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service

class WeightedMetricSpec extends AnyWordSpec with Matchers {

  implicit val rtW: Writes[MetricType] = MetricType.format

  "MetricType" should {

    "write ReadMe" in {
      Json.toJson(GithubMetricType: MetricType) shouldBe JsString("github")
    }

    "write LeakDetection" in {
      Json.toJson(LeakDetectionMetricType: MetricType) shouldBe JsString("leak-detection")
    }

    "write BobbyRule" in {
      Json.toJson(BobbyRuleMetricType: MetricType) shouldBe JsString("bobby-rule")
    }

    "write BuildStability" in {
      Json.toJson(BuildStabilityMetricType: MetricType) shouldBe JsString("build-stability")
    }

  }

  "Breakdown" should {
    implicit val sW: Writes[Breakdown] = Breakdown.writes
    "write correct json" in {
      val score = Breakdown(10, "Foo", Some("www.google.com"))

      Json.toJson(score) shouldBe Json.parse("""{"points":10,"description":"Foo","link":"www.google.com"}""")
    }
  }

  "WeightedMetric" should {
    implicit val rW: Writes[WeightedMetric] = WeightedMetric.writes
    "write correct json" in {
      val score  = Breakdown(10, "Foo", Some("www.google.com"))
      val rating = WeightedMetric(GithubMetricType, 10, Seq(score))
      Json.toJson(rating) shouldBe Json.parse(
      """{"metricType":"github",
          |"score":10,
          |"breakdown":[{"points":10,
          |"description":"Foo",
          |"link":"www.google.com"}]
          |}""".stripMargin)
    }
  }

  "Indicator" should {
    implicit val sW: Writes[Indicator] = Indicator.writes
    "write correct json" in {
      val score            = Breakdown(10, "Foo", Some("www.google.com"))
      val rating           = WeightedMetric(GithubMetricType, 10, Seq(score))
      val repositoryRating = Indicator("foo", Service, 10, Seq(rating))
      Json.toJson(repositoryRating) shouldBe Json.parse(
      """{"repoName":"foo",
          |"repoType":"Service",
          |"overallScore":10,
          |"weightedMetrics":[{
          |   "metricType":"github",
          |   "score":10,
          |   "breakdown":[{
          |       "points":10,
          |       "description":"Foo",
          |       "link":"www.google.com"
          |   }]
          |}]
          |}""".stripMargin)
    }
  }
}
