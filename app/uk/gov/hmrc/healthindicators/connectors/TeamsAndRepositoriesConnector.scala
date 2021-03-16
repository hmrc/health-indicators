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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, Reads, __}
import uk.gov.hmrc.healthindicators.configs.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.libs.functional.syntax._
import play.api.mvc.QueryStringBindable
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject() (
  httpClient: HttpClient,
  healthIndicatorsConfig: AppConfig
)(implicit val ec: ExecutionContext) {

  private val teamsAndRepositoriesBaseUrl: String = healthIndicatorsConfig.teamsAndRepositoriesUrl

  def allRepositories(implicit hc: HeaderCarrier): Future[List[TeamsAndRepos]] = {
    implicit val reads: Reads[TeamsAndRepos] = TeamsAndRepos.reads
    httpClient.GET[List[TeamsAndRepos]](s"$teamsAndRepositoriesBaseUrl/api/repositories")
  }

  def getJenkinsUrl(repo: String)(implicit hc: HeaderCarrier): Future[Option[JenkinsUrl]] = {
    implicit val reads: Reads[JenkinsUrl] = JenkinsUrl.juF
    httpClient.GET[Option[JenkinsUrl]](s"$teamsAndRepositoriesBaseUrl/api/jenkins-url/$repo")
  }
}

sealed trait RepositoryType

object RepositoryType {
  case object Service extends RepositoryType
  case object Prototype extends RepositoryType
  case object Library extends RepositoryType
  case object Other extends RepositoryType

  val format: Format[RepositoryType] = new Format[RepositoryType] {
    override def reads(json: JsValue): JsResult[RepositoryType] =
      json.validate[String].flatMap {
        case "Service"   => JsSuccess(Service)
        case "Prototype" => JsSuccess(Prototype)
        case "Library"   => JsSuccess(Library)
        case "Other"     => JsSuccess(Other)
        case s           => JsError(s"Invalid RepositoryType: $s")
      }

    override def writes(o: RepositoryType): JsValue =
      o match {
        case Service   => JsString("Service")
        case Prototype => JsString("Prototype")
        case Library   => JsString("Library")
        case Other     => JsString("Other")
        case s         => JsString(s"$s")
      }
  }

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]) =
    new QueryStringBindable[RepositoryType] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, RepositoryType]] = {
        val repositoryTypeString = params.get(key)
        repositoryTypeString.map({
          case Seq("Service")   => Right(Service)
          case Seq("Prototype") => Right(Prototype)
          case Seq("Library")   => Right(Library)
          case Seq("Other")     => Right(Other)
          case _                => Left("unable to bind RepositoryType from url")
        })
      }

      override def unbind(key: String, repositoryType: RepositoryType): String =
        stringBinder.unbind("repositoryType", repositoryType.toString)
    }
}

case class TeamsAndRepos(
  name: String,
  repositoryType: RepositoryType
)

object TeamsAndRepos {
  val reads: Reads[TeamsAndRepos] = {
    implicit val rtF: Format[RepositoryType] = RepositoryType.format
    ((__ \ "name").read[String]
      ~ (__ \ "repoType").format[RepositoryType])(TeamsAndRepos.apply _)
  }
}

case class JenkinsUrl(jenkinsURL: String)

object JenkinsUrl {
  implicit val juF: Format[JenkinsUrl] = Json.format[JenkinsUrl]
}
