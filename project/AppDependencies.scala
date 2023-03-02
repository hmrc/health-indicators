import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapPlayVersion = "7.14.0"
  val hmrcMongoVersion     = "0.73.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                  % "2.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-28"     % bootstrapPlayVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion     % Test,
    "org.mockito"       %% "mockito-scala"              % "1.16.23"            % Test
  )
}
