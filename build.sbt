name := "TManager"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.0.0-RC1",
  "mysql" % "mysql-connector-java" % "5.1.28",
  "org.apache.poi" % "poi" % "3.9",
  "org.apache.poi" % "poi-ooxml" % "3.9",
//  "org.slf4j" % "slf4j-nop" % "1.6.4",
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings

requireJs += "config.js"

lessEntryPoints <<= (sourceDirectory in Compile)(base => (
  (base / "assets" / "stylesheets" / "bootstrap" / "bootstrap.less") +++
    (base / "assets" / "stylesheets" / "upgrade.browser.less") +++
    (base / "assets" / "stylesheets" / "main.less")
  ))

//requireJs += "main.js"
//
//requireJsShim += "main.js
