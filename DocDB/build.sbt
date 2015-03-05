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

libraryDependencies += "net.lingala.zip4j" % "zip4j" % "1.2.3"

libraryDependencies += "com.h2database" % "h2" % "1.4.186"

play.Project.playJavaSettings
