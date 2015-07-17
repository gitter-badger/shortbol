lazy val sharedSettings = Seq(
  scalaVersion := "2.11.6",
  organization := "uk.co.turingatemyhamster",
  version := "0.0.1")


lazy val core = crossProject.settings(
  name := "shortbol-core",
  libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.3.0",
  libraryDependencies += "com.lihaoyi" %% "fastparse" % "0.1.7",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
  ).settings(sharedSettings : _*)
  
lazy val coreJs = core.js
lazy val coreJVM = core.jvm.settings(packAutoSettings : _*)

lazy val server = crossProject.settings(
  name := "shortbol-server"
  ).settings(sharedSettings : _*).dependsOn(core)

lazy val serverJs = server.js

lazy val serverJvm = server.jvm.settings(
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0-RC3"
  )
)

lazy val root = Project(
  id = "shortbol",
  base = file(".")) aggregate (serverJs, serverJvm)
