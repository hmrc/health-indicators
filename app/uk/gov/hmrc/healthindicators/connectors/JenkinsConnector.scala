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

import java.time.Instant

import com.google.common.io.BaseEncoding
import javax.inject.Inject
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.configs.JenkinsConfig
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class JenkinsConnector @Inject() (config: JenkinsConfig, http: HttpClient) {

  import JenkinsApiReads._

  def getBuildJob(baseUrl: String)(implicit ec: ExecutionContext): Future[Option[JenkinsBuildReport]] = {

    // Stops Server Side Request Forgery
    assert(baseUrl.startsWith(config.jenkinsHost))

    val authorizationHeader =
      s"Basic ${BaseEncoding.base64().encode(s"${config.username}:${config.token}".getBytes("UTF-8"))}"

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val url                        = url"${baseUrl}api/json?depth=1&tree=lastCompletedBuild[result,timestamp]"

    http
      .GET[Option[JenkinsBuildReport]](
        url = url,
        headers = Seq("Authorization" -> authorizationHeader)
      )
  }
}

case class JenkinsBuildReport(lastCompletedBuild: Option[JenkinsBuildStatus])
case class JenkinsBuildStatus(result: String, timeStamp: Instant)

object JenkinsApiReads {
  implicit val readsInstant: Reads[Instant] = (json: JsValue) => json.validate[Long].map(Instant.ofEpochMilli)

  implicit val jenkinsBuildStatus: Reads[JenkinsBuildStatus] =
    ((__ \ "result").read[String]
      ~ (__ \ "timestamp").read[Instant])(JenkinsBuildStatus.apply _)

  implicit val jenkinsBuildReport: Reads[JenkinsBuildReport] = Json.reads[JenkinsBuildReport]

}
