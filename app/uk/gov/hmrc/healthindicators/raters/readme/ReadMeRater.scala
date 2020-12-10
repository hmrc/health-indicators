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

package uk.gov.hmrc.healthindicators.raters.readme

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.healthindicators.models.{Rater, Rating}
import uk.gov.hmrc.healthindicators.raters.readme.ReadMeType.{DefaultReadMe, NoReadMe, ValidReadMe}

import scala.concurrent.{ExecutionContext, Future}

class ReadMeRater @Inject()(
  githubConnector: GithubConnector
)(implicit val ec: ExecutionContext)
    extends Rater {

  override def rate(repo: String): Future[Rating] = {
    Logger.info(s"Rating ReadMe for: $repo")
    validateReadMe(repo)
  }

  def validateReadMe(repo: String): Future[ReadMeRating] =
    for {
      response <- githubConnector.findReadMe(repo)

      readMeType = response match {
        // No README 404
        case None => NoReadMe
        // README Contains Default Text
        case Some(x) if x.contains("This is a placeholder README.md for a new repository") =>
          DefaultReadMe
        // README Valid
        case Some(x) => ValidReadMe
      }

      result = ReadMeRating(readMeType)
    } yield result
}
