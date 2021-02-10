package uk.gov.hmrc.healthindicators.connectors

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, JsSuccess, Json, Reads}

class LeakDetectionConnectorSpec
  extends AnyWordSpec
    with Matchers {
  "ReportLine" should {
    implicit val rlR: Reads[ReportLine] = ReportLine.reads
    "parse json" in {
      val jsonInput =
        """{"filePath": "/s3/src/test/resources/keystore.jks",
          "scope": "fileName",
          "lineNumber": 1,
          "urlToSource": "https://github.com/hmrc/alpakka/blame/96c3cdf1542b96a82bee10c8a2339f282b7230a0/s3/src/test/resources/keystore.jks#L1",
          "ruleId": "filename_private_key_11",
          "description": "Extension indicates Java KeyStore file, often containing private keys",
          "lineText": "keystore.jks"}"""
      val objectOutput = Json.parse(jsonInput).validate[ReportLine]
      objectOutput shouldBe
        JsSuccess(ReportLine(
          "/s3/src/test/resources/keystore.jks",
          "fileName",
          1,
          "https://github.com/hmrc/alpakka/blame/96c3cdf1542b96a82bee10c8a2339f282b7230a0/s3/src/test/resources/keystore.jks#L1",
          Some("filename_private_key_11"),
          "Extension indicates Java KeyStore file, often containing private keys",
          "keystore.jks"
        ))
    }
  }
}
