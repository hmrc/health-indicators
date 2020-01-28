package uk.gov.hmrc.healthindicators.raters

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.GithubConnector
import uk.gov.hmrc.healthindicators.model.ReadMeRating
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


class ReadMeRaterSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockGithubConnector = mock[GithubConnector]
  val service = new ReadMeRater(mockGithubConnector)

  implicit val hc = HeaderCarrier()

  "ReadMeService" should {

    "Return ReadMeRating Object with 'No README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMe404)

      val result = service.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(0, 0, "No README found")
    }

    "Return ReadMeRating Object with 'Deafult README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMeDeafult)

      val result = service.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(0, 52, "Default README found")
    }

    "Return ReadMeRating Object with 'Valid README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMeValid)

      val result = service.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(100, 25, "Valid README found")
    }
  }
}

object TestData {
  val readMe404     = HttpResponse(404, None, Map.empty, None)
  val readMeDeafult = HttpResponse(200, None, Map.empty, Some("This is a placeholder README.md for a new repository"))
  val readMeValid   = HttpResponse(200, None, Map.empty, Some("This is a valid README.md"))
}
