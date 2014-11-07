name := "DocDB"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)

libraryDependencies += "org.apache.tika" % "tika" % "1.6"

play.Project.playJavaSettings
