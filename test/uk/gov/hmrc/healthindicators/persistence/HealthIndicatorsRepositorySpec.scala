package uk.gov.hmrc.healthindicators.persistence

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.mockito.MockitoSugar
import org.mongodb.scala.ReadPreference
import org.mongodb.scala.model.IndexModel
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.models.HealthIndicators
import uk.gov.hmrc.mongo.test.DefaultMongoCollectionSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HealthIndicatorsRepositorySpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with DefaultMongoCollectionSupport {

  private lazy val repo = new HealthIndicatorsRepository(mongoComponent) {
    def findAll(): Future[Seq[HealthIndicators]] =
      collection.withReadPreference(ReadPreference.secondaryPreferred).find().toFuture().map(_.toList)
  }

  override protected def collectionName: String   = repo.collectionName
  override protected def indexes: Seq[IndexModel] = repo.indexes

  "HealthIndicatorsRepository.insertOne" should {

    val healthIndicators = HealthIndicators("test", Instant.now, Seq.empty)

    "insert correctly" in {
      repo.insertOne(healthIndicators)
      repo.findAll().futureValue mustBe Seq(healthIndicators)
    }
  }

  "HealthIndicatorsRepository.insert" should {

    val healthIndicators = HealthIndicators("test", Instant.now, Seq.empty)

    "insert correctly" in {
      repo.insert(Seq(healthIndicators, healthIndicators, healthIndicators))
      repo.findAll().futureValue must have size 3
    }
  }

  "HealthIndicatorsRepository.latestIndicators" should {

    val latest = HealthIndicators("test", Instant.now, Seq.empty)
    val older  = HealthIndicators("test", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val oldest = HealthIndicators("test", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest indicators for repo" in {
      repo.insert(Seq(latest, older, oldest))
      repo.latestIndicators("test").futureValue mustBe Some(latest)
    }

    "return none if no indicators are found for repo" in {
      repo.insert(Seq(latest, older, oldest))
      repo.latestIndicators("notfound").futureValue mustBe None
    }
  }

  "HealthIndicatorsRepository.latestIndicatorsAllRepos" should {

    val fooLatest = HealthIndicators("foo", Instant.now, Seq.empty)
    val fooOlder  = HealthIndicators("foo", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val fooOldest = HealthIndicators("foo", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    val barLatest = HealthIndicators("bar", Instant.now, Seq.empty)
    val barOlder  = HealthIndicators("bar", Instant.now.minus(1, ChronoUnit.DAYS), Seq.empty)
    val barOldest = HealthIndicators("bar", Instant.now.minus(2, ChronoUnit.DAYS), Seq.empty)

    "return the latest indicators for all repos" in {
      repo.insert(Seq(fooLatest, fooOlder, fooOldest, barLatest, barOlder, barOldest))
      repo.latestIndicatorsAllRepos().futureValue must contain theSameElementsAs Seq(fooLatest, barLatest)
    }
  }
}
