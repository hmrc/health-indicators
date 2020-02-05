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

package uk.gov.hmrc.healthindicators.raters.leakdetection

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Collector, Rating}
import uk.gov.hmrc.http.HeaderCarrier
import cats.data.OptionT
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class LeakDetectionCollector @Inject()(
  leakDetectionConnector: LeakDetectionConnector
)(implicit val ec: ExecutionContext)
    extends Collector {

  private implicit val hc = HeaderCarrier()

  override def rate(repo: String): Future[Rating] = countLeakDetections(repo)

  def countLeakDetections(repo: String): Future[LeakDetectionRating] =
    OptionT(leakDetectionConnector.findLatestMasterReport(repo))
      .map(_.inspectionResults.length)
      .getOrElse(0)
      .map(LeakDetectionRating(_))
}
