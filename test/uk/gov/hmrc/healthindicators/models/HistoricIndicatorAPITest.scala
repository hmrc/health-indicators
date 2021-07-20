package uk.gov.hmrc.healthindicators.models

import org.mockito.MockitoSugar
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant
import java.time.temporal.ChronoUnit

class HistoricIndicatorAPITest extends AnyWordSpec with Matchers with MockitoSugar with Eventually {

  private val now = Instant.now()

  private val singleHistoricIndicator = Seq(HistoricIndicator("test", now, 100))

  private val multipleHistoricIndicators = Seq(HistoricIndicator("test", now, 100),
    HistoricIndicator("test", now.plus(1, ChronoUnit.DAYS), 200),
    HistoricIndicator("test", now.plus(2, ChronoUnit.DAYS), 150))

  private val historicIndicatorAPISingleDataPoint = HistoricIndicatorAPI("test", Seq(DataPoint(now, 100)))

  private val historicIndicatorAPIMultipleDataPoints = HistoricIndicatorAPI("test", Seq(DataPoint(now, 100),
    DataPoint(now.plus(1, ChronoUnit.DAYS), 200),
    DataPoint(now.plus(2, ChronoUnit.DAYS), 150)))



  "HistoricIndicatorAPI.fromHistoricIndicators" should {

    "return None when Seq is Empty" in {
      HistoricIndicatorAPI.fromHistoricIndicators(Seq.empty) shouldBe None
    }

    "return HistoricIndicatorAPI with a single Data Point" in {
      HistoricIndicatorAPI.fromHistoricIndicators(singleHistoricIndicator) shouldBe Some(historicIndicatorAPISingleDataPoint)
    }

    "return HistoricIndicatorAPI with multiple Data Points" in {
      HistoricIndicatorAPI.fromHistoricIndicators(multipleHistoricIndicators) shouldBe Some(historicIndicatorAPIMultipleDataPoints)
    }
  }
}


