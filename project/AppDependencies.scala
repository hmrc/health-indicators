import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.4.0"
  val hmrcMongoVersion     = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-30"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                  % "2.10.0"
  )

  val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"     % bootstrapPlayVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion     % Test,
    "org.mockito"       %% "mockito-scala"              % "1.17.29"            % Test
  )

  val it = Seq.empty
}
