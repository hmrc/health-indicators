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

package uk.gov.hmrc.healthindicators.services

import java.time.Instant

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepositoryRatingServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepository: HealthIndicatorsRepository = mock[HealthIndicatorsRepository]

  private val bobbyRulesRating: Indicator =
    Indicator(BobbyRuleIndicatorType, Seq(Result(BobbyRuleActive, "desc", None)))
  private val readMeRating: Indicator = Indicator(ReadMeIndicatorType, Seq(Result(NoReadme, "desc", None)))
  private val leakDetectionRating: Indicator =
    Indicator(LeakDetectionIndicatorType, Seq(Result(LeakDetectionViolation, "desc", None)))
  private val healthIndicator: RepositoryHealthIndicator =
    RepositoryHealthIndicator("foo", Instant.now(), Seq(bobbyRulesRating, readMeRating, leakDetectionRating))

  private val scoreConfig       = new ScoreConfig
  private val repoScorerService = new RepositoryRatingService(mockRepository, scoreConfig)

  "repoScore" should {

    "Return a Total Score based on Ratings and Weights" in {
      when(mockRepository.latestRepositoryHealthIndicators("foo"))
        .thenReturn(Future.successful(Some(healthIndicator)))

      val result = repoScorerService.rateRepository("foo")

      result.futureValue mustBe Some(
        RepositoryRating(
          "foo",
          -200,
          Some(
            Seq(
              Rating(BobbyRule, -100, Seq(Score(-100, "desc", None))),
              Rating(ReadMe, -50, Seq(Score(-50, "desc", None))),
              Rating(LeakDetection, -50, Seq(Score(-50, "desc", None)))
            )
          )
        )
      )
    }
  }
}
