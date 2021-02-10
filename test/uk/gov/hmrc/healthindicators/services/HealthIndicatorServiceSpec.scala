package uk.gov.hmrc.healthindicators.services

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.scalatest.concurrent.{Eventually, IntegrationPatience}
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.concurrent.Waiters.scaled
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{TeamsAndRepos, TeamsAndRepositoriesConnector}
import uk.gov.hmrc.healthindicators.models.{Indicator, ReadMeIndicatorType, RepositoryHealthIndicator, Result, ValidReadme}
import uk.gov.hmrc.healthindicators.persistence.HealthIndicatorsRepository
import uk.gov.hmrc.healthindicators.raters.{Rater, ReadMeRater}
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
  val mockRater: Rater = mock[Rater]
  val raterList = List(mockRater)
  val healthIndicatorsRepository: HealthIndicatorsRepository = mock[HealthIndicatorsRepository]

  val healthIndicatorService = new HealthIndicatorService(
    teamsAndRepositoriesConnector,
    raterList,
    healthIndicatorsRepository)

  "insertHealthIndicators" should {
    "traverse all repos and create a repository indicator for each, inserting them into a mongo repo" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(List(
          TeamsAndRepos("repo1"),
          TeamsAndRepos("repo2"),
          TeamsAndRepos("repo3")))

      when(mockRater.rate(any)) thenReturn
        Future.successful(Indicator(ReadMeIndicatorType, Seq(Result(ValidReadme, "bar", None))))

      when(healthIndicatorsRepository.insert(any)) thenReturn Future.successful(Unit)

      Await.result(healthIndicatorService.insertHealthIndicators(), 10.seconds) shouldBe ()

      verify(healthIndicatorsRepository, times(3)).insert(any)
    }

    "does not insert any repository ratings when teamsAndRepositoriesConnector returns an empty list" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      when(teamsAndRepositoriesConnector.allRepositories) thenReturn
        Future.successful(List())

      when(mockRater.rate(any)) thenReturn
        Future.successful(Indicator(ReadMeIndicatorType, Seq(Result(ValidReadme, "bar", None))))

      when(healthIndicatorsRepository.insert(any)) thenReturn Future.successful(Unit)

      Await.result(healthIndicatorService.insertHealthIndicators(), 10.seconds) shouldBe ()
      verify(healthIndicatorsRepository, times(0)).insert(any)
    }

  }
}
