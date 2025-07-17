import sbt.*

object Dependencies {

  object Versions {
    val cats       = "2.13.0"
    val catsEffect = "3.6.2"
    val fs2        = "3.12.0"
    val http4s     = "0.23.9"
    val circe      = "0.14.2"
    val pureConfig = "0.17.9"

    val kindProjector    = "0.13.3"
    val logback          = "1.5.18"
    val scalaCheck       = "1.18.1"
    val scalaTest        = "3.2.19"
    val catsScalaCheck   = "0.3.2"
    val sttpClient       = "4.0.0-M9"
    val betterMonadicFor = "0.3.1"
    val catsRetry        = "3.1.3"
    val log4catsVersion  = "2.7.1"
    val catsEffectTest   = "2.1.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID      = "io.circe"                      %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID     = "org.http4s"                    %% artifact % Versions.http4s
    def sttpClient(artifact: String): ModuleID = "com.softwaremill.sttp.client4" %% artifact % Versions.sttpClient

    lazy val cats       = "org.typelevel" %% "cats-core"   % Versions.cats
    lazy val catsEffect = "org.typelevel" %% "cats-effect" % Versions.catsEffect
    lazy val fs2        = "co.fs2"        %% "fs2-core"    % Versions.fs2

    lazy val http4sDsl       = http4s("http4s-dsl")
    lazy val http4sServer    = http4s("http4s-blaze-server")
    lazy val http4sCirce     = http4s("http4s-circe")
    lazy val circeCore       = circe("circe-core")
    lazy val circeGeneric    = circe("circe-generic")
    lazy val circeGenericExt = circe("circe-generic-extras")
    lazy val circeParser     = circe("circe-parser")
    lazy val sttpClientCore  = sttpClient("core")
    lazy val sttpCirce       = sttpClient("circe")
    lazy val sttpBackend     = sttpClient("async-http-client-backend-cats")
    lazy val pureConfig      = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    lazy val catsRetry       = "com.github.cb372" %% "cats-retry" % Versions.catsRetry
    lazy val log4catsSlf4j   = "org.typelevel" %% "log4cats-slf4j" % Versions.log4catsVersion

    // Compiler plugins
    lazy val kindProjector    = "org.typelevel" %% "kind-projector"     % Versions.kindProjector cross CrossVersion.full
    lazy val betterMonadicFor = "com.olegpy"    %% "better-monadic-for" % Versions.betterMonadicFor

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"         % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"        % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck"   % Versions.catsScalaCheck
    lazy val catsEffectTest = "org.typelevel"     %% "munit-cats-effect" % Versions.catsEffectTest
  }

}
