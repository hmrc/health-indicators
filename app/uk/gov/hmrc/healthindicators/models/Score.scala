package uk.gov.hmrc.healthindicators.models

import play.api.libs.json._

import java.time.format.DateTimeFormatter

import cats.Applicative
import cats.implicits._
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json._
import uk.gov.hmrc.healthindicators.models.RatingType
import uk.gov.hmrc.healthindicators.raters.bobbyrules.{BobbyRuleViolations, Dependencies, Report}
import play.api.libs.functional.syntax._
import uk.gov.hmrc.healthindicators.configs.ScoreConfig
import uk.gov.hmrc.healthindicators.configs.WeightsConfig.readJson
import uk.gov.hmrc.healthindicators.raters.bobbyrules.BobbyRulesRating.getClass



case class Score (activeViolation: Int, pendingViolation: Int, leakDetections: Int)

object Score {
  val reads: Reads[Score] = {
    ((__ \ "activeViolation").read[Int]
      ~ (__ \ "pendingViolation").read[Int]
      ~ (__ \ "leakDetections").read[Int])(Score.apply _)
  }}
