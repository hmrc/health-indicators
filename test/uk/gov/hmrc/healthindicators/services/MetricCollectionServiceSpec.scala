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
import uk.gov.hmrc.healthindicators.connectors.RepoType.Service
import uk.gov.hmrc.healthindicators.connectors.{TeamsAndRepos, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.models.{CleanGithub, GithubMetricType, Metric, Result}
import uk.gov.hmrc.healthindicators.persistence.RepositoryMetricsRepository
import uk.gov.hmrc.healthindicators.metricproducers.MetricProducer
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class MetricCollectionServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with Eventually {
  implicit val defaultPatienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = scaled(Span(100, Seconds)),
      interval = scaled(Span(1000, Millis))
    )

  val teamsAndRepositoriesConnector: TeamsAndRepositoriesConnector = mock[TeamsAndRepositoriesConnector]
  val mockProducer: MetricProducer                                 = mock[MetricProducer]
  val producerList                                                 = List(mockProducer)
  val repositoryMetricsRepository: RepositoryMetricsRepository     = mock[RepositoryMetricsRepository]

  val metricCollectionService =
    new MetricCollectionService(teamsAndRepositoriesConnector, producerList, repositoryMetricsRepository)

  "metricCollectionService.collectAll" should {
    "traverse all repos and create a RepositoryMetric for each, inserting them into a mongo collection" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(
          List(TeamsAndRepos("repo1", Service), TeamsAndRepos("repo2", Service), TeamsAndRepos("repo3", Service))
        )

      when(mockProducer.produce(any)) thenReturn
        Future.successful(Metric(GithubMetricType, Seq(Result(CleanGithub, "bar", None))))

      when(repositoryMetricsRepository.insert(any, any)) thenReturn Future.successful(Unit)

      Await.result(metricCollectionService.collectAll(), 10.seconds) shouldBe ((): Unit)

      verify(repositoryMetricsRepository, times(3)).insert(any, any)
    }

    "not insert any RepositoryMetrics when teamsAndRepositoriesConnector returns an empty list" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(List())

      when(mockProducer.produce(any)) thenReturn
        Future.successful(Metric(GithubMetricType, Seq(Result(CleanGithub, "bar", None))))

      Await.result(metricCollectionService.collectAll(), 10.seconds) shouldBe ((): Unit)
    }

  }
}
