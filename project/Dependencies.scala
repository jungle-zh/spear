import sbt._

object Dependencies {
  val extraResolvers = Seq(
    Resolver.mavenLocal,
    "Twitter Maven" at "http://maven.twttr.com"
  )

  object Versions {
    val ammonite = "1.0.1"
    val ammonite = "1.0.0"
    val avatica = "1.10.0"
    val config = "1.2.1"
    val fastparse = "0.4.4"
    val log4j = "1.2.16"
    val mockito = "2.8.47"
    val scala = "2.12.3"
    val scalaCheck = "1.13.5"
    val scalaTest = "3.0.4"
    val scalaXml = "1.0.6"
    val scopt = "3.6.0"
    val slf4j = "1.6.4"

    val fansi = "0.2.4"
    val sourcecode = "0.1.3"
  }

  val ammonite = Seq(
    "com.lihaoyi" % s"ammonite_${Versions.scala}" % Versions.ammonite
  )

  val avatica = Seq(
    "org.apache.calcite.avatica" % "avatica" % Versions.avatica
  )

  val fastparse = Seq(
    "com.lihaoyi" %% "fastparse" % Versions.fastparse
  )

  val log4j = Seq(
    "log4j" % "log4j" % Versions.log4j
  )

  val mockito = Seq(
    "org.mockito" % "mockito-core" % Versions.mockito % "test"
  )

  val scala = Seq(
    "org.scala-lang" % "scala-library" % Versions.scala,
    "org.scala-lang" % "scala-reflect" % Versions.scala,
    "org.scala-lang.modules" %% "scala-xml" % Versions.scalaXml
  )

  val scalaCheck = Seq(
    "org.scalacheck" %% "scalacheck" % Versions.scalaCheck % "test"
  )

  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalaTest % "test"
  )

  val scopt = Seq(
    "com.github.scopt" %% "scopt" % Versions.scopt
  )

  val slf4j = Seq(
    "org.slf4j" % "slf4j-api" % Versions.slf4j,
    "org.slf4j" % "slf4j-log4j12" % Versions.slf4j,
    "org.slf4j" % "jul-to-slf4j" % Versions.slf4j
  )

  val sourcecode = Seq(
    "com.lihaoyi" %% "sourcecode" % Versions.sourcecode
  )

  val testing: Seq[ModuleID] = mockito ++ scalaCheck ++ scalaTest

  val typesafeConfig = Seq(
    "com.typesafe" % "config" % Versions.config
  )

  val logging: Seq[ModuleID] = log4j ++ slf4j

  val overrides: Seq[ModuleID] = scala ++ sourcecode ++ scopt
}
