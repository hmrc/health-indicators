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
import uk.gov.hmrc.healthindicators.connectors.RepositoryType.Service
import uk.gov.hmrc.healthindicators.models.RatingType.{BobbyRule, LeakDetection, ReadMe}
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RepositoryRatingServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockRepository: HealthIndicatorsRepository = mock[HealthIndicatorsRepository]

  private val bobbyRulesRating: Indicator =
    Indicator(BobbyRuleIndicatorType, Seq(Result(BobbyRuleActive, "desc", None)))
  private val readMeRatingOne: Indicator = Indicator(ReadMeIndicatorType, Seq(Result(NoReadme, "desc", None)))
  private val readMeRatingTwo: Indicator = Indicator(ReadMeIndicatorType, Seq(Result(ValidReadme, "desc", None)))
  private val leakDetectionRating: Indicator =
    Indicator(LeakDetectionIndicatorType, Seq(Result(LeakDetectionViolation, "desc", None)))
  private val healthIndicatorOne: RepositoryHealthIndicator =
    RepositoryHealthIndicator("foo", Instant.now(), Service, Seq(bobbyRulesRating, readMeRatingOne, leakDetectionRating))
  private val healthIndicatorTwo: RepositoryHealthIndicator =
    RepositoryHealthIndicator("bar", Instant.now(), Service, Seq(bobbyRulesRating, readMeRatingTwo, leakDetectionRating))

  private val scoreConfig       = new ScoreConfig
  private val repoScorerService = new RepositoryRatingService(mockRepository, scoreConfig)

  "repoScore" should {

    "Return a Rating for a single Repository" in {
      when(mockRepository.latestRepositoryHealthIndicators("foo"))
        .thenReturn(Future.successful(Some(healthIndicatorOne)))

      val result = repoScorerService.rateRepository("foo")

      result.futureValue mustBe Some(
        RepositoryRating(
          "foo",
          Service,
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

    "Return a Rating for each Repository in ascending order" in {
      when(mockRepository.latestAllRepositoryHealthIndicators(None))
        .thenReturn(Future.successful(Seq(healthIndicatorOne, healthIndicatorTwo)))

      val result = repoScorerService.rateAllRepositories(repoType = None, SortType.Ascending)

      result.futureValue mustBe Seq(
        RepositoryRating(
          "foo",
          Service,
          -200,
          Some(
            Seq(
              Rating(BobbyRule, -100, Seq(Score(-100, "desc", None))),
              Rating(ReadMe, -50, Seq(Score(-50, "desc", None))),
              Rating(LeakDetection, -50, Seq(Score(-50, "desc", None)))
            )
          )
        ),RepositoryRating(
          "bar",
          Service,
          -100,
          Some(
            Seq(
              Rating(BobbyRule, -100, Seq(Score(-100, "desc", None))),
              Rating(ReadMe, 50, Seq(Score(50, "desc", None))),
              Rating(LeakDetection, -50, Seq(Score(-50, "desc", None)))
            )
          )
        )
      )
    }

    "Return a Rating for each Repository in descending order, when sort equals true" in {
      when(mockRepository.latestAllRepositoryHealthIndicators(None))
        .thenReturn(Future.successful(Seq(healthIndicatorTwo, healthIndicatorOne)))

      val result = repoScorerService.rateAllRepositories(repoType = None, SortType.Descending)

      result.futureValue mustBe Seq(
        RepositoryRating(
          "bar",
          Service,
          -100,
          Some(
            Seq(
              Rating(BobbyRule, -100, Seq(Score(-100, "desc", None))),
              Rating(ReadMe, 50, Seq(Score(50, "desc", None))),
              Rating(LeakDetection, -50, Seq(Score(-50, "desc", None)))
            )
          )
        ),  RepositoryRating(
          "foo",
          Service,
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



