package uk.gov.hmrc.healthindicators.models

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{Writes, _}

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
      val score = Score(10, "Foo", Some("www.google.com"))
      val rating = Rating(ReadMe, 10, Seq(score))
      Json.toJson(rating) mustBe Json.parse(
        """{"ratingType":"ReadMe",
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
      val score = Score(10, "Foo", Some("www.google.com"))
      val rating = Rating(ReadMe, 10, Seq(score))
      val repositoryRating = RepositoryRating("foo", 10, Some(Seq(rating)))
      Json.toJson(repositoryRating) mustBe Json.parse(
        """{"repositoryName":"foo",
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
