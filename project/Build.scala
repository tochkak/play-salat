import com.typesafe.sbteclipse.plugin.EclipsePlugin.EclipseKeys
import sbt.Keys._
import sbt._

object ProjectBuild extends Build {

  lazy val buildVersion = "1.7.3"
  lazy val playVersion = "2.6.6"
  lazy val cashbahVersion = "3.1.1"
  lazy val salatVersion = "1.11.2"

  lazy val root = Project(id = "play-plugins-salat",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ Publish.settings).settings(
    organization := "ru.tochkak",
    description := "MongoDB Salat plugin for PlayFramework 2",
    version := buildVersion,
    scalaVersion := "2.12.4",
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
      "com.github.salat" %% "salat" % salatVersion,
      "org.mongodb" %% "casbah-gridfs" % cashbahVersion
    )
  )
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
    homepage := Some(url("https://github.com/tochkak/play-salat")),
    pomExtra := (
      <scm>
        <url>https://github.com/tochkak/play-salat</url>
        <connection>scm:git:git@github.com:tochkak/play-salat.git</connection>
      </scm>
        <developers>
          <developer>
            <id>shayanlinux</id>
            <name>Shayan Shahand</name>
            <url>http://github.com/shayanlinux</url>
          </developer>
          <developer>
            <id>panshin</id>
            <name>Gleb Panshin</name>
            <url>http://panshin.pro</url>
          </developer>
        </developers>))
}
