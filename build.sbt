name := "creek-xml"

version := "0.1.8"
organization := "com.github.rgafiyatullin"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
scalacOptions ++= Seq("-language:implicitConversions")
scalacOptions ++= Seq("-Ywarn-value-discard", "-Xfatal-warnings")

publishTo := {
  val nexus = "http://am3-v-perftest-xmppcs-1.be.core.pw:8081/"
  Some("releases"  at nexus + "content/repositories/sbt-releases/")
}
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials.am3")

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6"
  )
