name := "TManager"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.0.0-RC1",
  "mysql" % "mysql-connector-java" % "5.1.28",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings
