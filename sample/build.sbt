import play.sbt.routes.RoutesKeys

name := "play-salat-sample"

scalaVersion := "2.11.8"

organization := "ru.tochkak"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ws" % "2.5.3",
  "ru.tochkak" %% "play-plugins-salat" % "1.7.0"
)

scalacOptions += "-deprecation"

scalacOptions += "-feature"

RoutesKeys.routesImport += "ru.tochkak.plugin.salat.Binders._"