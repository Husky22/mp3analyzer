ThisBuild / scalaVersion     := "3.3.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.orbital"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "MP3Analyzer",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.19",
      "dev.zio" %% "zio-test" % "2.0.19" % Test,
      "dev.zio" %% "zio-http" % "3.0.0-RC3",
      "org.scodec" %% "scodec-core" % "2.2.1",
      "org.scodec" %% "scodec-bits" % "1.1.37",
      "dev.zio" %% "zio-json" % "0.6.2",
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
