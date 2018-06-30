lazy val root = project
  .in(file("."))
  .aggregate(
    upicklemagnoliaJS, 
    upicklemagnoliaJVM
  )
  .settings(
    publish := {},
    publishLocal := {}
  )



lazy val upicklemagnolia = crossProject
  .in(file("."))
  .settings(
    name := "upickle-magnolia",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-deprecation", 
      "-feature",
      "-Xlint"
    ),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    libraryDependencies ++= Seq(
      "com.propensive" %%% "magnolia" % "0.8.0",
      "com.lihaoyi" %%% "upickle" % "0.6.6",
      "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  //.jvmSettings()
  //.jsSettings()

lazy val upicklemagnoliaJVM = upicklemagnolia.jvm
lazy val upicklemagnoliaJS = upicklemagnolia.js

