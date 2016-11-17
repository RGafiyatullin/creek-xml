name := "creek-xml"

version := "0.1.1"
organization := "com.github.rgafiyatullin"

publishTo := {
  val nexus = "http://nexus.in-docker.localhost:8081/"
  Some("releases"  at nexus + "content/repositories/releases")
}
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6"
  )
