import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapPlayVersion = "5.7.0"
  val hmrcMongoVersion = "0.51.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-28"  % bootstrapPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"         % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                  % "2.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"     % hmrcMongoVersion    % Test,
    "org.scalatest"          %% "scalatest"                   % "3.1.2"             % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"          % "5.1.0"             % Test,
    "com.typesafe.play"      %% "play-test"                   % PlayVersion.current % Test,
    "org.mockito"            %% "mockito-scala"               % "1.16.23"            % Test,
    "com.github.tomakehurst" %  "wiremock"                    % "1.58"              % Test
  )

}
