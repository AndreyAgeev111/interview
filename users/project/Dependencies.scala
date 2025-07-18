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
    val betterMonadicFor = "0.3.1"
    val logback          = "1.5.18"
    val scalaCheck       = "1.18.1"
    val scalaTest        = "3.2.19"
    val catsScalaCheck   = "0.3.2"
    val derevo           = "0.14.0"
    val lens             = "1.9.12"
    val log4catsVersion  = "2.7.1"
  }

  object Libraries {
    def circe(artifact: String): ModuleID  = "io.circe"   %% artifact % Versions.circe
    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

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
    lazy val pureConfig      = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    lazy val derevo          = "tf.tofu" %% "derevo-circe" % Versions.derevo exclude ("io.circe", "circe-core_2.13")
    lazy val lens            = "com.softwaremill.quicklens" %% "quicklens" % Versions.lens
    lazy val log4catsSlf4j   = "org.typelevel" %% "log4cats-slf4j" % Versions.log4catsVersion

    // Compiler plugins
    lazy val kindProjector    = "org.typelevel" %% "kind-projector"     % Versions.kindProjector cross CrossVersion.full
    lazy val betterMonadicFor = "com.olegpy"    %% "better-monadic-for" % Versions.betterMonadicFor

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    lazy val scalaTest      = "org.scalatest"     %% "scalatest"       % Versions.scalaTest
    lazy val scalaCheck     = "org.scalacheck"    %% "scalacheck"      % Versions.scalaCheck
    lazy val catsScalaCheck = "io.chrisdavenport" %% "cats-scalacheck" % Versions.catsScalaCheck
  }

}
