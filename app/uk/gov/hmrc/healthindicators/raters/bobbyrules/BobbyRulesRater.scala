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

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.models.{Rater, Rating}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.Logger
import scala.concurrent.{ExecutionContext, Future}

class BobbyRulesRater @Inject()(
   bobbyRuleConnector: BobbyRuleConnector
   )(implicit val ec: ExecutionContext)
extends Rater {

    private implicit val hc = HeaderCarrier()

    override def rate(repo: String): Future[Rating] = {
        Logger.info(s"Rating LeakDetection for: $repo")
        countViolationsForRepo(repo)
    }

    def countViolationsForRepo(repo: String): Future[BobbyRulesRating] = {
        for {
            bobbyRuleReport: Option[Report] <- bobbyRuleConnector.findLatestMasterReport(repo)
            dependencies: Seq[Dependencies] = bobbyRuleReport.map(b => {
                b.libraryDependencies ++ b.sbtPluginsDependencies ++ b.otherDependencies
            }).getOrElse(Seq())
            violationCount: Int = dependencies.map(_.bobbyRuleViolations.size).sum

        } yield BobbyRulesRating(violationCount)
    }
}
