import sbt.Keys._

name := "csim_root"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions += "-deprecation"

enablePlugins(ScalaJSPlugin)

lazy val root = project.in(file(".")).aggregate(csimJS, csimJVM)

lazy val csim = crossProject.in(file(".")).
  settings(
    name := "csim",
    version := "0.0.3"
  ).
  jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      "net.sf.jopt-simple" % "jopt-simple" % "5.0.3"
    ),
    mainClass := Some("me.assil.csim.Main"),
    mainClass in assembly := Some("me.assil.csim.Main")
  ).
  jsSettings(
    scalaJSUseRhino in Global := false,
    mainClass := Some("me.assil.csim.Csim")
  )

lazy val csimJS = csim.js
lazy val csimJVM = csim.jvm
