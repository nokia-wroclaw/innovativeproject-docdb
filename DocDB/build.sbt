name := "DocDB"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
	"org.apache.tika" % "tika" % "0.3"
)


libraryDependencies += "org.elasticsearch" % "elasticsearch" % "1.4.0"

libraryDependencies += "org.apache.tika" % "tika-parsers" % "1.6"

libraryDependencies += "junit" % "junit" % "4.11"

play.Project.playJavaSettings
