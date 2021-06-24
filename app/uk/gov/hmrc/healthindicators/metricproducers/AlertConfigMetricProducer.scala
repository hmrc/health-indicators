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

package uk.gov.hmrc.healthindicators.metricproducers

import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.{AlertConfig, ServiceConfigsConnector}
import uk.gov.hmrc.healthindicators.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AlertConfigMetricProducer @Inject() (serviceConfigConnector: ServiceConfigsConnector)(implicit
  val ec: ExecutionContext
) extends MetricProducer {

  private val logger = Logger(this.getClass)

  override def produce(repo: String): Future[Metric] = {
    logger.debug(s"Metric Alert Config for: $repo")
    serviceConfigConnector.findAlertConfigs(repo).map { response =>
      Metric(AlertConfigMetricType, getResults(response))
    }
  }

  private def getResults(maybeConfig: Option[AlertConfig]): Seq[Result] =
    maybeConfig match {
      case None                     => createResults(AlertConfigNotFound, "No Alert Config Found")
      case Some(AlertConfig(true))  => createResults(AlertConfigEnabled, "Alert Config is Enabled")
      case Some(AlertConfig(false)) => createResults(AlertConfigDisabled, "Alert Config is Disabled")
    }

  private def createResults(result: AlertConfigResultType, description: String): Seq[Result] =
    Seq(Result(result, description, None))
}
