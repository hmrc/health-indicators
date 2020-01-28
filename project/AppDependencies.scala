import sbt._

object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.3.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-26"       % "0.21.0"

  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.3.0"                 % Test classifier "tests",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test"          % "0.21.0"                % Test,
    "org.scalatest"           %% "scalatest"                % "3.1.0"                 % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.3"                 % Test,
    "com.typesafe.play"       %% "play-test"                % PlayVersion.current     % Test,
    "org.mockito"             %% "mockito-scala"            % "1.10.2"                % Test
  )

}
