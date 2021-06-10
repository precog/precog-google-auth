ThisBuild / crossScalaVersions := Seq("2.12.10")
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / githubRepository := "precog-google-auth"

ThisBuild / homepage := Some(url("https://github.com/precog/precog-google-auth"))

ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/precog/precog-google-auth"),
  "scm:git@github.com:precog/precog-google-auth.git"))

ThisBuild / publishAsOSSProject := true

val ArgonautVersion = "6.3.2"
val CatsEffectVersion = "2.2.0"
val GoogleAuthVersion = "0.25.0"

// Include to also publish a project's tests
lazy val publishTestsSettings = Seq(
  Test / packageBin / publishArtifact := true)

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "precog-google-auth",
    libraryDependencies ++= Seq(
      "com.google.auth" % "google-auth-library-oauth2-http" % GoogleAuthVersion,
      "io.argonaut" %% "argonaut" % ArgonautVersion,
      "org.typelevel" %% "cats-effect" % CatsEffectVersion
    ))

