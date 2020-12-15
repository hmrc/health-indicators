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

package uk.gov.hmrc.healthindicators.raters.bobbyrules

import cats.data.OptionT
import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Rater, Rating}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class BobbyRulesRater @Inject()(
   bobbyRuleConnector: BobbyRuleConnector
   )(implicit val ec: ExecutionContext)
extends Rater {

    private implicit val hc = HeaderCarrier()

    override def rate(repo: String): Int = {//Future[Rating] = {
        Logger.info(s"Rating LeakDetection for: $repo")
        countViolationsForRepo(repo)
}

    def countViolationsForRepo(repo: String): Int = {
        val result = bobbyRuleConnector.findLatestMasterReport(repo)
        var count = 0
        val report = Await.result(result, 10 seconds).get
        report.libraryDependencies.foreach(dependencies => {
            count += dependencies.bobbyRuleViolations.length
        })
        count
    }
}
