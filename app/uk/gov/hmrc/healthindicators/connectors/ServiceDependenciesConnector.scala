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

package uk.gov.hmrc.healthindicators.connectors

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsValue, Reads, __}
import uk.gov.hmrc.healthindicators.configs.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ServiceDependenciesConnector @Inject() (
  httpClient: HttpClient,
  ratersConfig: AppConfig
)(implicit val ec: ExecutionContext) {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val serviceDependenciesBaseURL: String = ratersConfig.serviceDependencies

  def dependencies(repo: String): Future[Option[Dependencies]] = {
    implicit val rF: Reads[Dependencies] = Dependencies.reads
    val logger                           = Logger(this.getClass)
    httpClient
      .GET[Option[Dependencies]](s"$serviceDependenciesBaseURL/api/dependencies/$repo")
      .recoverWith {
        case UpstreamErrorResponse.Upstream5xxResponse(x) =>
          logger.error(s"An error occurred when connecting to $repo: ${x.getMessage()}", x)
          Future.successful(None)
      }
  }
}

case class BobbyRuleViolation(
  reason: String,
  from: LocalDate,
  range: String
)

object BobbyRuleViolation {
  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  implicit val readsDate: Reads[LocalDate] = (json: JsValue) =>
    json.validate[String].map(LocalDate.parse(_, dateFormatter))

  val reads: Reads[BobbyRuleViolation] =
    ((__ \ "reason").read[String]
      ~ (__ \ "from").read[LocalDate]
      ~ (__ \ "range").read[String])(BobbyRuleViolation.apply _)
}

case class Dependency(
  bobbyRuleViolations: Seq[BobbyRuleViolation],
  name: String
)

object Dependency {
  val reads: Reads[Dependency] = {
    implicit val brvR: Reads[BobbyRuleViolation] = BobbyRuleViolation.reads
    ((__ \ "bobbyRuleViolations").read[Seq[BobbyRuleViolation]]
      ~ (__ \ "name").read[String])(Dependency.apply _)
  }
}

case class Dependencies(
  repositoryName: String,
  libraryDependencies: Seq[Dependency],
  sbtPluginsDependencies: Seq[Dependency],
  otherDependencies: Seq[Dependency]
)

object Dependencies {
  val reads: Reads[Dependencies] = {
    implicit val ldR: Reads[Dependency] = Dependency.reads
    ((__ \ "repositoryName").read[String]
      ~ (__ \ "libraryDependencies").read[Seq[Dependency]]
      ~ (__ \ "sbtPluginsDependencies").read[Seq[Dependency]]
      ~ (__ \ "otherDependencies").read[Seq[Dependency]])(Dependencies.apply _)
  }
}
