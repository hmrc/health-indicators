import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val hmrcMongoVersion = "0.33.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-backend-play-27"  % "4.0.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-27"         % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                  % "1.6.1"
  )

  val test = Seq(
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-27"     % hmrcMongoVersion    % Test,
    "org.scalatest"          %% "scalatest"                   % "3.1.2"             % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"          % "4.0.0"             % Test,
    "com.typesafe.play"      %% "play-test"                   % PlayVersion.current % Test,
    "org.mockito"            %% "mockito-scala"               % "1.10.2"            % Test,
    "com.github.tomakehurst" %  "wiremock"                    % "1.58"              % Test
  )

}
