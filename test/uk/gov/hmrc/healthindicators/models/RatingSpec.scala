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

package uk.gov.hmrc.healthindicators.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Writes, _}
import uk.gov.hmrc.healthindicators.connectors.RepositoryType.Service
import uk.gov.hmrc.healthindicators.models.RatingType.{BobbyRule, BuildStability, LeakDetection, OpenPR, ReadMe}

class RatingSpec extends AnyWordSpec with Matchers {

  implicit val rtW: Writes[RatingType] = RatingType.writes

  "RatingType" should {

    "write ReadMe" in {
      Json.toJson(ReadMe) mustBe JsString("ReadMe")
    }

    "write LeakDetection" in {
      Json.toJson(LeakDetection) mustBe JsString("LeakDetection")
    }

    "write BobbyRule" in {
      Json.toJson(BobbyRule) mustBe JsString("BobbyRule")
    }

    "write BuildStability" in {
      Json.toJson(BuildStability) mustBe JsString("BuildStability")
    }

    "write OpenPRs" in {
      Json.toJson(OpenPR) mustBe JsString("OpenPR")
    }
  }

  "Score" should {
    implicit val sW: Writes[Score] = Score.writes
    "write correct json" in {
      val score = Score(10, "Foo", Some("www.google.com"))

      Json.toJson(score) mustBe Json.parse("""{"points":10,"description":"Foo","ratings":"www.google.com"}""")
    }
  }

  "Rating" should {
    implicit val rW: Writes[Rating] = Rating.writes
    "write correct json" in {
      val score  = Score(10, "Foo", Some("www.google.com"))
      val rating = Rating(ReadMe, 10, Seq(score))
      Json.toJson(rating) mustBe Json.parse("""{"ratingType":"ReadMe",
          |"ratingScore":10,
          |"breakdown":[{"points":10,
            |"description":"Foo",
            |"ratings":"www.google.com"}]
          |}""".stripMargin)
    }
  }

  "RepositoryRating" should {
    implicit val sW: Writes[RepositoryRating] = RepositoryRating.writes
    "write correct json" in {
      val score            = Score(10, "Foo", Some("www.google.com"))
      val rating           = Rating(ReadMe, 10, Seq(score))
      val repositoryRating = RepositoryRating("foo", Service, 10, Seq(rating))
      Json.toJson(repositoryRating) mustBe Json.parse("""{"repositoryName":"foo",
          |"repositoryType":"Service",
          |"repositoryScore":10,
          |"ratings":[{
          |   "ratingType":"ReadMe",
          |   "ratingScore":10,
          |   "breakdown":[{
          |       "points":10,
          |       "description":"Foo",
          |       "ratings":"www.google.com"
          |   }]
          |}]
          |}""".stripMargin)
    }
  }
}
