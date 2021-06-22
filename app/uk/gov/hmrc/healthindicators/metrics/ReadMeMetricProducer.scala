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

package uk.gov.hmrc.healthindicators.metrics

import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.GithubConnector
import uk.gov.hmrc.healthindicators.models._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReadMeMetricProducer @Inject()(
  githubConnector: GithubConnector
)(implicit val ec: ExecutionContext)
    extends MetricProducer {

  private val logger: Logger = Logger(this.getClass)

  override def produce(repo: String): Future[Metric] = {
    logger.debug(s"Metric ReadMe for: $repo")
    githubConnector.findReadMe(repo).map { response =>
      Metric(ReadMeMetricType, getResults(response))
    }
  }

  private def getResults(readme: Option[String]): Seq[Result] =
    readme match {
      case Some(str) if str.contains("This is a placeholder README.md for a new repository") =>
        createResults(DefaultReadme, "Default readme")
      case None    => createResults(NoReadme, "No Readme defined")
      case Some(_) => createResults(ValidReadme, "Valid readme")
    }

  private def createResults(result: ReadMeResultType, description: String): Seq[Result] =
    Seq(Result(result, description, None))
}
