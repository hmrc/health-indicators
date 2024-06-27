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

package uk.gov.hmrc.healthindicators.connectors

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TeamsAndRepositoriesConnector @Inject() (
  httpClientV2  : HttpClientV2,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {
  import HttpReads.Implicits._

  private val teamsAndRepositoriesBaseUrl: String =
    servicesConfig.baseUrl("teams-and-repositories")

  def allRepositories(implicit hc: HeaderCarrier): Future[List[TeamsAndRepos]] = {
    implicit val reads: Reads[TeamsAndRepos] = TeamsAndRepos.reads
    httpClientV2
      .get(url"$teamsAndRepositoriesBaseUrl/api/repositories")
      .execute[List[TeamsAndRepos]]
  }
}

sealed trait RepoType {
  val asString: String
}
object RepoType {
  case object Service   extends RepoType { override val asString = "Service"   }
  case object Prototype extends RepoType { override val asString = "Prototype" }
  case object Library   extends RepoType { override val asString = "Library"   }
  case object Test      extends RepoType { override val asString = "Test"      }
  case object Other     extends RepoType { override val asString = "Other"     }

  val format: Format[RepoType] = new Format[RepoType] {
    override def reads(json: JsValue): JsResult[RepoType] =
      json.validate[String].flatMap {
        case Service.asString   => JsSuccess(Service)
        case Prototype.asString => JsSuccess(Prototype)
        case Library.asString   => JsSuccess(Library)
        case Test.asString      => JsSuccess(Test)
        case Other.asString     => JsSuccess(Other)
        case s                  => JsError(s"Invalid RepositoryType: $s")
      }

    override def writes(o: RepoType): JsValue =
      JsString(o.asString)
  }

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[RepoType] =
    new QueryStringBindable[RepoType] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, RepoType]] = {
        val repositoryTypeString = params.get(key)
        repositoryTypeString.map({
          case Seq(Service.asString)   => Right(Service)
          case Seq(Prototype.asString) => Right(Prototype)
          case Seq(Library.asString)   => Right(Library)
          case Seq(Test.asString)      => Right(Test)
          case Seq(Other.asString)     => Right(Other)
          case _                       => Left("unable to bind RepositoryType from url")
        })
      }

      override def unbind(key: String, repositoryType: RepoType): String =
        stringBinder.unbind("repositoryType", repositoryType.toString)
    }
}

case class TeamsAndRepos(
  name          : String,
  repositoryType: RepoType
)

object TeamsAndRepos {
  val reads: Reads[TeamsAndRepos] = {
    implicit val rtr: Reads[RepoType] = RepoType.format
    ( (__ \ "name"    ).read[String]
    ~ (__ \ "repoType").read[RepoType]
    )(TeamsAndRepos.apply _)
  }
}

case class JenkinsUrl(asString: String)

object JenkinsUrl {
  implicit val jur: Reads[JenkinsUrl] =
    (__ \ "jenkinsURL").read[String].map(JenkinsUrl.apply)
}
