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

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.connectors.GithubConnector
import uk.gov.hmrc.healthindicators.models._

import scala.concurrent.{ExecutionContext, Future}

class ReadMeRater @Inject()(
                             githubConnector: GithubConnector,
                           )(implicit val ec: ExecutionContext)
  extends Rater {

  private val logger: Logger = Logger(this.getClass)

  override def rate(repo: String): Future[Seq[Indicator]] = {
    logger.info(s"Rating ReadMe for: $repo")
    githubConnector.findReadMe(repo).map { response =>
      Seq(functionX(response))
    }
  }

  //todo rename
  private def functionX(readme: Option[String]): Indicator = {
    readme match {
      case Some(x) if x.contains("This is a placeholder README.md for a new repository") =>
        createIndicator(DefaultReadme, "Default readme")
      case None => createIndicator(NoReadme, "No Readme defined")
      case Some(_) => createIndicator(ValidReadme, "Valid readme")
    }
  }

  private def createIndicator(result: ReadMeIndicatorType, description: String): Indicator = Indicator(ReadMeIndicator(result), description, None)
}
