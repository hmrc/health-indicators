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

import java.time.LocalDate

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{BobbyRuleViolation, Dependencies, Dependency, ServiceDependenciesConnector}
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BobbyRulesRaterSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockBobbyRulesConnector = mock[ServiceDependenciesConnector]
  private val rater                   = new BobbyRulesRater(mockBobbyRulesConnector)

  private val dependencyWithActiveViolation: Dependency =
    Dependency(Seq(BobbyRuleViolation("reason", LocalDate.parse("1994-01-08"), "range")), "name")

  private val dependencyWithPendingViolation: Dependency =
    Dependency(Seq(BobbyRuleViolation("reason", LocalDate.now.plusDays(1), "range")), "name")

  private val dependencyWithoutViolation: Dependency = Dependency(Seq(), "name")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "rate" should {

    "Return Indicator with no results when repo is not found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(None)

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(BobbyRuleIndicatorType, Seq.empty)
    }

    "Return Indicator with no results when a report with no bobby rules is found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(
        Some(Dependencies("repoName", Seq(), Seq(), Seq()))
      )

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(BobbyRuleIndicatorType, Seq.empty)
    }

    "Return Indicator with active violation result when bobby violation is found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(
        Some(
          Dependencies(
            "repoName",
            Seq(dependencyWithActiveViolation),
            Seq(dependencyWithoutViolation),
            Seq(dependencyWithoutViolation)
          )
        )
      )

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(BobbyRuleIndicatorType, Seq(Result(BobbyRuleActive, "name - reason", None)))
    }

    "Return Indicator with pending violation result when pending bobby violation is found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(
        Some(
          Dependencies(
            "repoName",
            Seq(dependencyWithPendingViolation),
            Seq(dependencyWithoutViolation),
            Seq(dependencyWithoutViolation)
          )
        )
      )

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(BobbyRuleIndicatorType, Seq(Result(BobbyRulePending, "name - reason", None)))

    }

    "Return Indicator with pending violation and 2 active results when bobby violations found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(
        Some(
          Dependencies(
            "repoName",
            Seq(dependencyWithPendingViolation),
            Seq(dependencyWithActiveViolation),
            Seq(dependencyWithActiveViolation)
          )
        )
      )

      val result = rater.rate("foo")

      result.futureValue mustBe Indicator(
        BobbyRuleIndicatorType,
        Seq(
          Result(BobbyRulePending, "name - reason", None),
          Result(BobbyRuleActive, "name - reason", None),
          Result(BobbyRuleActive, "name - reason", None)
        )
      )
    }
  }
}
