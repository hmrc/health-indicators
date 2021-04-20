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

import uk.gov.hmrc.healthindicators.connectors.{GithubConnector, OpenPR}
import uk.gov.hmrc.healthindicators.models._

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StalePrRater @Inject()(
                              githubConnector: GithubConnector
                            )(
                              implicit val ec: ExecutionContext
                            )
  extends Rater {

  override def rate(repo: String): Future[Indicator] = {
    githubConnector.getOpenPRs(repo)
      .map(getResultType).map(result => {
      Indicator(OpenPRIndicatorType, result)
    })
  }

  def getResultType(maybeOpenPRs: Option[Seq[OpenPR]]): Seq[Result] = {
    maybeOpenPRs match {
      case None => Seq(Result(PRsNotFound, "PR information could not be found", None))
      case Some(Seq()) => Seq(Result(NoOpenPRs, s"No Open PRs", None))
      case Some(openPRs) => openPRs.map(isStale)
    }
  }

  def isStale(openPR: OpenPR): Result = {
    val prStalenessDays = 30

    if (ChronoUnit.DAYS.between(openPR.updatedAt, LocalDate.now()) > prStalenessDays) {
      Result(StalePR, s"${openPR.title}: PR older than $prStalenessDays days", None)
    }

    else Result(FreshPR, s"${openPR.title}: Minty fresh PR", None)
  }
}
