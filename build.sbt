name := "wvscrap"
version := "1.0"

scalaVersion := "2.12.7"

val Versions = new {
    val Circe = "0.11.1"
    val HtmlCleaner = "2.4"
    val ScalaScraper = "2.1.0"
    val LogBack = "1.2.3"
    val ScalaLogging = "3.9.2"
}

libraryDependencies ++= Seq(
    "net.sourceforge.htmlcleaner" % "htmlcleaner" % Versions.HtmlCleaner,
    "net.ruippeixotog" %% "scala-scraper" % Versions.ScalaScraper,
    "ch.qos.logback" % "logback-classic" % Versions.LogBack,
    "com.typesafe.scala-logging" %% "scala-logging" % Versions.ScalaLogging,
    "io.circe" %% "circe-core" % Versions.Circe,
    "io.circe" %% "circe-generic" % Versions.Circe,
    "io.circe" %% "circe-parser" % Versions.Circe
)
