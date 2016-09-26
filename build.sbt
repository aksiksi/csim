name := "csim_root"

scalaVersion in ThisBuild := "2.11.8"

enablePlugins(ScalaJSPlugin)

lazy val root = project.in(file(".")).aggregate(csimJS, csimJVM)

lazy val csim = crossProject.in(file(".")).
  settings(
    name := "csim",
    version := "0.0.1",
    mainClass in (Compile, run) := Some("me.assil.Main")
  ).
  jvmSettings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  ).
  jsSettings(
    scalaJSUseRhino in Global := false
  )

lazy val csimJS = csim.js
lazy val csimJVM = csim.jvm