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

package uk.gov.hmrc.healthindicators.raters

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.connectors.LeakDetectionConnector
import uk.gov.hmrc.healthindicators.models.{Indicator, IndicatorType, LeakDetectionIndicator, LeakDetectionViolation}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class LeakDetectionRater @Inject() (
  leakDetectionConnector: LeakDetectionConnector,
)(implicit val ec: ExecutionContext)
    extends Rater {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val logger                     = Logger(this.getClass)

  override def rate(repo: String): Future[Seq[Indicator]] = {
    logger.info(s"Rating LeakDetection for: $repo")
    leakDetectionConnector.findLatestMasterReport(repo).map { maybeReport =>
      maybeReport.toSeq.flatMap{ report =>
        report.inspectionResults.map{ reportLine =>
          Indicator(LeakDetectionIndicator(LeakDetectionViolation), reportLine.description, Some(reportLine.urlToSource))
        }
      }
    }
  }
}
