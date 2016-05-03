import play.sbt.PlayScala
import sbt._
import Keys._
import play.sbt.PlayImport._
import play.twirl.sbt.Import._
import play.sbt.routes.RoutesKeys
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object ApplicationBuild extends Build {

  val appName = "sample"
  val appVersion = "1.0"

  val appDependencies = Seq(
    "com.typesafe.play" %% "play-ws" % "2.5.3",
    "net.cloudinsights" %% "play-plugins-salat" % "1.6.0")

  val main = Project(appName, file(".")).enablePlugins(PlayScala).settings(
    version := appVersion,
    scalaVersion := "2.11.7",
    libraryDependencies ++= appDependencies,
    RoutesKeys.routesImport += "se.radley.plugin.salat.Binders._",
    TwirlKeys.templateImports += "org.bson.types.ObjectId",
    resolvers += Resolver.sonatypeRepo("snapshots"),
    EclipseKeys.withSource := true,
    EclipseKeys.withJavadoc := true)

}
