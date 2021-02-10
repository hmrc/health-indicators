package uk.gov.hmrc.healthindicators.connectors

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json, Reads}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ServiceDependenciesConnectorSpec extends AnyWordSpec with Matchers{
  "BobbyRuleViolation" should {
    implicit val brvR: Reads[BobbyRuleViolation] = BobbyRuleViolation.reads
    "parse json correctly" in {
      val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val jsonInput = """{"reason": "reason", "from": "1999-01-01", "range": "range"}"""
      val objectOutput = Json.parse(jsonInput).validate[BobbyRuleViolation]
      objectOutput shouldBe
        JsSuccess(BobbyRuleViolation(
          "reason",
          LocalDate.parse("1999-01-01", dateFormatter),
          "range"))
    }
  }
}
