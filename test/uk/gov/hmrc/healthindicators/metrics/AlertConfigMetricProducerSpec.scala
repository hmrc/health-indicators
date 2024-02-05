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

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{AlertConfig, ServiceConfigsConnector}
import uk.gov.hmrc.healthindicators.models.{AlertConfigDisabled, AlertConfigEnabled, AlertConfigMetricType, AlertConfigNotFound}
import uk.gov.hmrc.healthindicators.metricproducers.AlertConfigMetricProducer
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlertConfigMetricProducerSpec
  extends AnyWordSpec
     with Matchers
     with MockitoSugar
     with ArgumentMatchersSugar
     with ScalaFutures {

  private val mockServiceConfigsConnector: ServiceConfigsConnector = mock[ServiceConfigsConnector]
  private val producer: AlertConfigMetricProducer = new AlertConfigMetricProducer(mockServiceConfigsConnector)

  "AlertConfigMetricProducer.produce" should {
    "Return a Metric with AlertConfigNotFound when service configs connector returns None" in {
      when(mockServiceConfigsConnector.findAlertConfigs(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = producer.produce("foo").futureValue

      result.metricType shouldBe AlertConfigMetricType
      result.results.head.resultType shouldBe AlertConfigNotFound
    }

    "Return a Metric with AlertConfigEnabled when a service has config enabled" in {

      when(mockServiceConfigsConnector.findAlertConfigs(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(AlertConfig(true))))

      val result = producer.produce("foo").futureValue

      result.metricType shouldBe AlertConfigMetricType
      result.results.head.resultType shouldBe AlertConfigEnabled
    }

    "Return a Metric with AlertConfigDisabled when a service has config disabled" in {

      when(mockServiceConfigsConnector.findAlertConfigs(eqTo("foo"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(AlertConfig(false))))

      val result = producer.produce("foo").futureValue

      result.metricType shouldBe AlertConfigMetricType
      result.results.head.resultType shouldBe AlertConfigDisabled
    }
  }
}
