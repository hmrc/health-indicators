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

package uk.gov.hmrc.healthindicators.metrics

import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.healthindicators.connectors.{AlertConfig, ServiceConfigsConnector}
import uk.gov.hmrc.healthindicators.models.{AlertConfigDisabled, AlertConfigEnabled, AlertConfigMetricType, AlertConfigNotFound}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AlertConfigMetricProducerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockServiceConfigsConnector: ServiceConfigsConnector = mock[ServiceConfigsConnector]
  private val rater: AlertConfigMetricProducer                              = new AlertConfigMetricProducer(mockServiceConfigsConnector)

  "rate" should {

    "Return an Indicator with AlertConfigNotFound when service configs connector returns None" in {
      when(mockServiceConfigsConnector.findAlertConfigs("foo")).thenReturn(Future.successful(None))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe AlertConfigMetricType
      result.results.head.resultType mustBe AlertConfigNotFound
    }

    "Return an Indicator with AlertConfigEnabled when a service has config enabled" in {

      when(mockServiceConfigsConnector.findAlertConfigs("foo")).thenReturn(Future.successful(Some(AlertConfig(true))))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe AlertConfigMetricType
      result.results.head.resultType mustBe AlertConfigEnabled

    }

    "Return an Indicator with AlertConfigDisabled when a service has config disabled" in {

      when(mockServiceConfigsConnector.findAlertConfigs("foo")).thenReturn(Future.successful(Some(AlertConfig(false))))

      val result = rater.produce("foo").futureValue

      result.metricType mustBe AlertConfigMetricType
      result.results.head.resultType mustBe AlertConfigDisabled

    }

  }
}
