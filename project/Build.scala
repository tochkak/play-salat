import sbt._
import sbt.Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys

object ProjectBuild extends Build {

  lazy val buildVersion = "1.6.0"
  lazy val playVersion = "2.5.3"

  lazy val root = Project(id = "play-plugins-salat",
    base = file("."),
    settings = Project.defaultSettings ++ Publish.settings).settings(
      organization := "net.cloudinsights",
      description := "MongoDB Salat plugin for PlayFramework 2",
      version := buildVersion,
      scalaVersion := "2.11.7",
      EclipseKeys.withSource := true,
      EclipseKeys.withJavadoc := true,
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
      parallelExecution in Test := false,
      testFrameworks += TestFrameworks.Specs2,

      resolvers ++= Seq(
        "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
        "play Repository" at "http://repo.typesafe.com/typesafe/simple/maven-releases/",
        Resolver.sonatypeRepo("releases"),
        Resolver.sonatypeRepo("snapshots")),

      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play" % playVersion % "provided",
        "com.typesafe.play" % "play-exceptions" % playVersion % "provided",
        "com.typesafe.play" %% "play-specs2" % playVersion % "test",
        "com.novus" %% "salat" % "1.9.9",
        "org.mongodb" %% "casbah" % "2.8.2"))
}

object Publish {
  lazy val settings = Seq(
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("sonatype snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("sonatype releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/cloudinsights/play-salat")),
    pomExtra := (
      <scm>
        <url>git://github.com/cloudinsights/play-salat.git</url>
        <connection>scm:git://github.com/cloudinsights/play-salat.git</connection>
      </scm>
      <developers>
        <developer>
          <id>amarjitmult</id>
          <name>Amarjit Singh</name>
          <url>http://github.com/cloudinsights</url>
        </developer>
      </developers>))
}
