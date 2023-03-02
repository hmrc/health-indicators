/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.healthindicators.metrics

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{Leak, LeakDetectionConnector}
import uk.gov.hmrc.healthindicators.models._
import uk.gov.hmrc.healthindicators.metricproducers.LeakDetectionMetricProducer
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LeakDetectionMetricProducerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockLeakDetectionConnector: LeakDetectionConnector = mock[LeakDetectionConnector]
  private val producer: LeakDetectionMetricProducer = new LeakDetectionMetricProducer(mockLeakDetectionConnector)


  implicit val hc: HeaderCarrier = HeaderCarrier()

  "LeakDetectionMetricProducer.produce" should {

    "Return a Metric with no results when leak detection connector returns None" in {
      when(mockLeakDetectionConnector.findLeaks("foo")).thenReturn(Future.successful(Nil))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(LeakDetectionMetricType, Seq(Result(LeakDetectionNotFound, "No Leaks Detected", None)))

    }

    "Return a Metric with no results when a Report with no violation is found" in {
      when(mockLeakDetectionConnector.findLeaks("foo"))
        .thenReturn(Future.successful(Nil))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(LeakDetectionMetricType, Seq(Result(LeakDetectionNotFound,"No Leaks Detected",None)))
    }

    "Return a Metric with a result when a Report with 1 violations is found" in {
      val leak = Leak("foo", "main", "rule1")
      when(mockLeakDetectionConnector.findLeaks("foo"))
        .thenReturn(Future.successful(Seq(leak)))

      val result = producer.produce("foo")

      result.futureValue mustBe Metric(
        LeakDetectionMetricType,
        Seq(Result(LeakDetectionViolation, s"Branch ${leak.branch} has an unresolved ${leak.ruleId} leak", None))
      )
    }

    "Return a Metric with 2 results when Report with 2 violations is found" in {
      when(mockLeakDetectionConnector.findLeaks("foo"))
        .thenReturn(Future.successful(Seq(Leak("foo", "main", "rule1"), Leak("foo", "main", "rule2"))))

      val result = producer.produce("foo").futureValue

      result.results.count(_.resultType == LeakDetectionViolation) mustBe 2
    }

    "Cap the total number of leaks reported" in {
      val lotsOfLeaks = Range(0,50).map(_ => Leak("foo", "main", "rule1"))
      when(mockLeakDetectionConnector.findLeaks("foo"))
        .thenReturn(Future.successful(lotsOfLeaks))

      val result = producer.produce("foo").futureValue

      result.results.count(_.resultType == LeakDetectionViolation) mustBe producer.maxLeaks+1
      result.results.last.description mustBe s"Has ${lotsOfLeaks.length-producer.maxLeaks} additional unresolved leaks."
    }

  }
}
