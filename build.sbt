name := "wvscrap"
version := "1.0"

scalaVersion := "2.12.7"

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.4"
libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.1.0"
val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
).map(_ % circeVersion)
