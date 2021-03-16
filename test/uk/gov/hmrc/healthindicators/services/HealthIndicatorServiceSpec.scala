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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.RepositoryType.Service
import uk.gov.hmrc.healthindicators.connectors.{TeamsAndRepos, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.models.{Indicator, ReadMeIndicatorType, Result, ValidReadme}
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.healthindicators.raters.Rater
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class HealthIndicatorServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with Eventually {
  implicit val defaultPatienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = scaled(Span(100, Seconds)),
      interval = scaled(Span(1000, Millis))
    )

  val teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
  val mockRater: Rater                                             = mock[Rater]
  val raterList                                                    = List(mockRater)
  val healthIndicatorsRepository: HealthIndicatorsRepository       = mock[HealthIndicatorsRepository]

  val healthIndicatorService =
    new HealthIndicatorService(teamsAndRepositoriesConnector, raterList, healthIndicatorsRepository)

  "insertHealthIndicators" should {
    "traverse all repos and create a repository indicator for each, inserting them into a mongo repo" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(
          List(TeamsAndRepos("repo1", Service), TeamsAndRepos("repo2", Service), TeamsAndRepos("repo3", Service))
        )

      when(mockRater.rate(any)) thenReturn
        Future.successful(Indicator(ReadMeIndicatorType, Seq(Result(ValidReadme, "bar", None))))

      when(healthIndicatorsRepository.insert(any)) thenReturn Future.successful(Unit)

      Await.result(healthIndicatorService.insertHealthIndicators(), 10.seconds) shouldBe ((): Unit)

      verify(healthIndicatorsRepository, times(3)).insert(any)
    }

    "not insert any repository ratings when teamsAndRepositoriesConnector returns an empty list" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(List())

      when(mockRater.rate(any)) thenReturn
        Future.successful(Indicator(ReadMeIndicatorType, Seq(Result(ValidReadme, "bar", None))))

      Await.result(healthIndicatorService.insertHealthIndicators(), 10.seconds) shouldBe ((): Unit)
    }

  }
}
