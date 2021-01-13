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

package uk.gov.hmrc.healthindicators.raters.bobbyrules

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
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{BobbyRuleViolations, Dependencies, Dependency, ServiceDependenciesConnector}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class BobbyRulesRaterSpec extends AnyWordSpec with Matchers with MockitoSugar {

  private val mockBobbyRulesConnector = mock[ServiceDependenciesConnector]
  private val rater                   = new BobbyRulesRater(mockBobbyRulesConnector)

  private val dependencyWithActiveViolation: Dependency =
    Dependency(Seq(BobbyRuleViolations("reason", LocalDate.parse("1994-01-08"), "range")), "name")

  private val dependencyWithPendingViolation: Dependency =
    Dependency(Seq(BobbyRuleViolations("reason", LocalDate.now.plusDays(1), "range")), "name")

  private val dependencyWithoutViolation: Dependency = Dependency(Seq(), "name")

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "BobbyRulesRater" should {

    "Return BobbyRulesRating Object with (0,0) Rating when repo is not found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(None)

      val dependencyList = rater.getDependencyList("foo")

      val result = dependencyList.map(a => rater.countViolationsForRepo(a))

      Await.result(result, 5.seconds) mustBe BobbyRulesRating(0, 0)
    }

    "Return BobbyRuleRating Object with (0, 0) Rating when a Report with 0 Dependencies is found" in {
      when(mockBobbyRulesConnector.dependencies("foo")) thenReturn Future.successful(
        Some(Dependencies("repoName", Seq(), Seq(), Seq()))
      )
      val dependencyList = rater.getDependencyList("foo")

      val result = dependencyList.map(a => rater.countViolationsForRepo(a))

      Await.result(result, 5.seconds) mustBe BobbyRulesRating(0, 0)
    }

    "Return BobbyRuleRating Object with (0, 1) Rating when a Report with 0 Pending and 1 Active Violation is found" in {
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

      val dependencyList = rater.getDependencyList("foo")

      val result = dependencyList.map(a => rater.countViolationsForRepo(a))

      Await.result(result, 5.seconds) mustBe BobbyRulesRating(0, 1)
    }

    "Return BobbyRule Rating Object with (1, 0) Rating when a Report with 1 Pending and 0 Actives Violation is found" in {
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

      val dependencyList = rater.getDependencyList("foo")

      val result = dependencyList.map(a => rater.countViolationsForRepo(a))

      Await.result(result, 5.seconds) mustBe BobbyRulesRating(1, 0)
    }

    "Return BobbyRule Rating Object (1, 2) Rating when a Report with 1 Pending and 2 Actives Violation is found" in {
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

      val dependencyList = rater.getDependencyList("foo")

      val result = dependencyList.map(a => rater.countViolationsForRepo(a))

      Await.result(result, 5.seconds) mustBe BobbyRulesRating(1, 2)
    }
  }
}
