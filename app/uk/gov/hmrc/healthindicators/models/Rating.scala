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

package uk.gov.hmrc.healthindicators.models

import java.net.URL

sealed trait RatingType

case object ReadMe extends RatingType

case object LeakDetection extends RatingType

case object BobbyRules extends RatingType


case class RepositoryRating(repositoryName: String, repositoryScore: Int, ratings: Seq[Rating])
case class Rating(ratingType: RatingType, ratingScore: Int, breakdown: Seq[Score])

case class Score(points: Int, description: String, href: Option[String])
