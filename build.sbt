name := "creek-xml"

version := "0.1.6"
organization := "com.github.rgafiyatullin"

publishTo := {
  val nexus = "http://am3-v-perftest-xmppcs-1.be.core.pw:8081/"
  Some("releases"  at nexus + "content/repositories/sbt-releases")
}
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6"
  )
