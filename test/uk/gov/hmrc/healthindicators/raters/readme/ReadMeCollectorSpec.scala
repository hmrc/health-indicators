/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.healthindicators.raters.readme

import org.mockito.MockitoSugar
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ReadMeCollectorSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockGithubConnector = mock[GithubConnector]
  val rater = new ReadMeCollector(mockGithubConnector)

  implicit val hc = HeaderCarrier()

  "ReadMeRater" should {

    "Return ReadMeRating Object with 'No README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMe404)

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(0, "No README found")
    }

    "Return ReadMeRating Object with 'Deafult README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMeDeafult)

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(52, "Default README found")
    }

    "Return ReadMeRating Object with 'Valid README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(TestData.readMeValid)

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(25, "Valid README found")
    }
  }
}

object TestData {
  val readMe404     = HttpResponse(404, None, Map.empty, None)
  val readMeDeafult = HttpResponse(200, None, Map.empty, Some("This is a placeholder README.md for a new repository"))
  val readMeValid   = HttpResponse(200, None, Map.empty, Some("This is a valid README.md"))
}
