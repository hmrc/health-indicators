/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.healthindicators.connectors.LeakDetectionConnector
import uk.gov.hmrc.healthindicators.model.{LeakDetectionRating, Rating}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class LeakDetectionRater @Inject()(leakDetectionConnector: LeakDetectionConnector)(implicit val ec: ExecutionContext) extends Rater {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def rate(repo: String): Future[Rating] = countLeakDetections(repo)

  def countLeakDetections(repo: String): Future[LeakDetectionRating] = {

    for {
      report <- leakDetectionConnector.findLatestMasterReport(repo)

      result = report match {
        // No Report
        case None =>
          LeakDetectionRating(100, 0)

        // Report Exists
        case Some(x) => x match {
          // 0 Violations
          case x if x.inspectionResults.isEmpty =>
            LeakDetectionRating(100, x.inspectionResults.length)
          // 1 Violation
          case x if x.inspectionResults.length == 1 =>
            LeakDetectionRating(50, x.inspectionResults.length)
          // 2+ Violations
          case x if x.inspectionResults.length >= 2 =>
            LeakDetectionRating(0, x.inspectionResults.length)
        }
      }
    } yield result

  }
}
