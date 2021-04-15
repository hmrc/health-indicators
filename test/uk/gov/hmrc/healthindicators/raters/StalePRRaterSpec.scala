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

package uk.gov.hmrc.healthindicators.raters

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{GithubConnector, OpenPR}
import uk.gov.hmrc.healthindicators.models.{FreshPR, NoOpenPRs, OpenPRIndicatorType, PRsNotFound, StalePR}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StalePRRaterSpec
  extends AnyWordSpec
  with Matchers
  with MockitoSugar
  with ScalaFutures
  with ArgumentMatchersSugar{

  private val mockGithubConnector: GithubConnector = mock[GithubConnector]
  private val rater: StalePRRater = new StalePRRater(mockGithubConnector)

  "StalePRRater.rate" should {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
    "return PRsNotFound when GithubConnector.getOpenPRs returns None" in {
      when(mockGithubConnector.getOpenPRs(eqTo("foo")))
        .thenReturn(Future.successful(None))

      val result = rater.rate("foo").futureValue

      result.indicatorType shouldBe OpenPRIndicatorType
      result.results.head.resultType shouldBe PRsNotFound
    }

    "return NoOpenPRs when GithubConnector.getOpenPRs returns Some(Seq.empty)" in {
      when(mockGithubConnector.getOpenPRs(eqTo("foo")))
        .thenReturn(Future.successful(Some(Seq.empty)))

      val result = rater.rate("foo").futureValue

      result.indicatorType shouldBe OpenPRIndicatorType
      result.results.head.resultType shouldBe NoOpenPRs
    }

    "return FreshPR when GithubConnector.getOpenPRs returns Some(Seq(OpenPR) with last edit less than 30 days" in {
      when(mockGithubConnector.getOpenPRs(eqTo("foo")))
        .thenReturn(Future.successful(Some(Seq(OpenPR("hello-world",
          LocalDate.parse("2021-04-16T13:38:36Z", dateFormatter),
          LocalDate.now
        )))))

      val result = rater.rate("foo").futureValue

      result.indicatorType shouldBe OpenPRIndicatorType
      result.results.head.resultType shouldBe FreshPR
    }

    "return StalePR when GithubConnector.getOpenPRs returns Some(Seq(OpenPR) with last edit more than 30 days" in {
      when(mockGithubConnector.getOpenPRs(eqTo("foo")))
        .thenReturn(Future.successful(Some(Seq(OpenPR("hello-world",
          LocalDate.parse("2021-04-16T13:38:36Z", dateFormatter),
          LocalDate.now.minusDays(31L)
        )))))

      val result = rater.rate("foo").futureValue

      result.indicatorType shouldBe OpenPRIndicatorType
      result.results.head.resultType shouldBe StalePR
    }

    "return 1 StalePR and 1 FreshPR when GithubConnector.getOpenPRs returns 2 OpenPRs, one with an edit in the last 30 days and the other without" in {
      when(mockGithubConnector.getOpenPRs(eqTo("foo")))
        .thenReturn(Future.successful(Some(Seq(
          OpenPR("hello-world-fresh",
            LocalDate.parse("2021-04-16T13:38:36Z", dateFormatter),
            LocalDate.now),
          OpenPR("hello-world-stale",
            LocalDate.parse("2021-04-16T13:38:36Z", dateFormatter),
            LocalDate.now.minusDays(31L)),
        ))))

      val result = rater.rate("foo").futureValue

      result.indicatorType shouldBe OpenPRIndicatorType
      result.results(0).resultType shouldBe FreshPR
      result.results(1).resultType shouldBe StalePR
    }
  }
}
