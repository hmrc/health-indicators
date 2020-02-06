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
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeType.{DefaultReadMe, NoReadMe, ValidReadMe}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ReadMeCollectorSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val mockGithubConnector = mock[GithubConnector]
  val rater               = new ReadMeCollector(mockGithubConnector)

  implicit val hc = HeaderCarrier()

  "ReadMeRater" should {

    "Return ReadMeRating Object with 'No README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(None)

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(NoReadMe)
    }

    "Return ReadMeRating Object with 'Deafult README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(Some(TestData.readMeDeafult))

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(DefaultReadMe)
    }

    "Return ReadMeRating Object with 'Valid README found message'" in {
      when(mockGithubConnector.findReadMe("foo")) thenReturn Future.successful(Some(TestData.readMeValid))

      val result = rater.validateReadMe("foo")

      Await.result(result, 5 seconds) mustBe ReadMeRating(ValidReadMe)
    }
  }
}

object TestData {
  val readMeDeafult = "This is a placeholder README.md for a new repository"
  val readMeValid   = "This is a valid README.md"
}
