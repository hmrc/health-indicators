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

package uk.gov.hmrc.healthindicators.raters

import javax.inject.Inject
import uk.gov.hmrc.healthindicators.connectors.GithubConnector
import uk.gov.hmrc.healthindicators.model.{Rating, ReadMeRating}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class ReadMeRater @Inject()(githubConnector: GithubConnector)(implicit val ec: ExecutionContext) extends Rater {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def rate(repo: String): Future[Rating] = validateReadMe(repo)

  def validateReadMe(repo: String): Future[ReadMeRating] = {

    for {
      response <- githubConnector.findReadMe(repo)

      result = response match {
        case response if response.status >= 400 =>
          ReadMeRating(
              rating  = 0
            , length = 0
            , message = "No README found"
          )
        case response if response.body.contains("This is a placeholder README.md for a new repository") =>
          ReadMeRating(
              rating  = 0
            , length = response.body.length
            , message = "Default README found"
          )
        case _ =>
          ReadMeRating(
              rating  = 100
            , length = response.body.length
            , message = "Valid README found"
          )
      }
    } yield result

  }
}

