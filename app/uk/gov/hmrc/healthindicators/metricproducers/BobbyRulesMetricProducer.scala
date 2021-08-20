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
import uk.gov.hmrc.healthindicators.connectors.{BobbyRuleViolation, ServiceDependenciesConnector}
import uk.gov.hmrc.healthindicators.models._

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BobbyRulesMetricProducer @Inject() (
  serviceDependenciesConnector: ServiceDependenciesConnector
)(implicit val ec: ExecutionContext)
    extends MetricProducer {

  private val logger = Logger(this.getClass)

  override def produce(repo: String): Future[Metric] = {
    logger.debug(s"Metric BobbyRules for: $repo")

    for {
      maybeDependencies <- serviceDependenciesConnector.dependencies(repo)
      allDependencies = maybeDependencies
                          .map(dependencies =>
                            dependencies.libraryDependencies ++
                              dependencies.sbtPluginsDependencies ++ dependencies.otherDependencies
                          )
                          .getOrElse(Seq.empty)
      allViolations = allDependencies.flatMap(d =>
                        d.bobbyRuleViolations
                          .map(v => Result(getResultType(v), s"${d.name} - ${v.reason}", None))
                      )

      groupViolations   = allViolations.partition(_.resultType == BobbyRuleActive)
      activeViolations  = groupViolations._1.foldLeft(Option.empty[Result])(mergeResult)
      pendingViolations = groupViolations._2.foldLeft(Option.empty[Result])(mergeResult)
      groupedViolations = activeViolations.toSeq ++ pendingViolations.toSeq

      result = if (groupedViolations.isEmpty) Seq(Result(NoActiveOrPending, "No Active or Pending Bobby Rules", None))
               else groupedViolations
    } yield Metric(BobbyRuleMetricType, result)

  }

  private def mergeResult(output: Option[Result], cur: Result): Option[Result] =
    if (output.isEmpty) Some(cur) else output.map(r => r.copy(description = r.description + "\n" + cur.description))

  private def getResultType(violation: BobbyRuleViolation): BobbyRuleResultType = {
    val now = LocalDate.now
    if (violation.from.isBefore(now))
      BobbyRuleActive
    else
      BobbyRulePending
  }
}
