import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

  val appName         = "submission"
  val appVersion      = "1.0-SNAPSHOT"



  val appDependencies = Seq(
    "journalio" % "journalio" % "1.2"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Journal.IO" at "https://raw.github.com/sbtourist/Journal.IO/master/m2/repo"
  )

}
