name := "creek-xml"

version := "0.1.7"
organization := "com.github.rgafiyatullin"

publishTo := {
  val nexus = "http://nexus.in-docker.localhost:8081/"
  Some("releases"  at nexus + "repository/my-releases")
}
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials.local")

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6"
  )
