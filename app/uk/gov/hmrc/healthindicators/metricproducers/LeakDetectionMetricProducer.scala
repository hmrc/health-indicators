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

package uk.gov.hmrc.healthindicators.metricproducers

import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.{LeakDetectionConnector}
import uk.gov.hmrc.healthindicators.models.{LeakDetectionMetricType, LeakDetectionNotFound, LeakDetectionViolation, Metric, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LeakDetectionMetricProducer @Inject() (
  leakDetectionConnector: LeakDetectionConnector
)(implicit val ec: ExecutionContext)
    extends MetricProducer {

  private val logger = Logger(this.getClass)

  final val maxLeaks = 10

  override def produce(repo: String): Future[Metric] = {
    logger.debug(s"Metric LeakDetection for: $repo")
    leakDetectionConnector.findLeaks(repo).map { reports =>
      val results =
        if (reports.isEmpty)
          Seq(Result(LeakDetectionNotFound, "No Leaks Detected", None))
        else {
          reports
            .take(maxLeaks)
            .map(r =>
              Result(LeakDetectionViolation, s"Branch ${r.branch} has an unresolved ${r.ruleId} leak", None)) ++ {
                  // limit the amount of leaks we report, once over the limit just summarize.
                  if(reports.length > maxLeaks) Seq(Result(LeakDetectionViolation, s"Has ${reports.length-maxLeaks} additional unresolved leaks.", None)) else Nil
            }
        }
      Metric(LeakDetectionMetricType, results)
    }
  }
}
