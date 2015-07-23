import sbt._
import Keys._
import play.Play.autoImport._
import play.Play
import play.sbt.PlayImport._
import play.twirl.sbt.Import._


object ApplicationBuild extends Build {

    val appName         = "sample"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.typesafe.play" %% "play-ws" % "2.4.2",
      "se.radley" %% "play-plugins-salat" % "1.5.1"
    )

    val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
      version :=appVersion,
      scalaVersion := "2.11.6",
      libraryDependencies ++= appDependencies,
      routesImport += "se.radley.plugin.salat.Binders._",
      TwirlKeys.templateImports += "org.bson.types.ObjectId",
      resolvers += Resolver.sonatypeRepo("snapshots")
    )

}
