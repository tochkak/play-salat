import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import play.twirl.sbt.Import.TwirlKeys

object ApplicationBuild extends Build {

    val appName         = "sample"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.typesafe.play" %% "play-ws" % "2.3.2",
      "se.radley" %% "play-plugins-salat" % "1.5.0"
    )

    val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
      version :=appVersion,
      libraryDependencies ++= appDependencies,
      routesImport += "se.radley.plugin.salat.Binders._",
      TwirlKeys.templateImports += "org.bson.types.ObjectId",
      resolvers += Resolver.sonatypeRepo("snapshots")
    )

}
