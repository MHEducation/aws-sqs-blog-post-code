// Name of the project and base package name
name := "sqs-blog"
organization := "com.mhedu"

scalaVersion := "2.12.8"

// Log Config
logLevel in run := Level.Warn
ivyLoggingLevel := UpdateLogging.Quiet

// Add Resolvers
resolvers ++= Seq(
  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/",
  "Central" at "https://repo1.maven.org/maven2/",
  "mvnrepository" at "https://mvnrepository.com/artifact",
  "Central" at "https://repo1.maven.org/maven2/",
  Resolver.typesafeRepo("releases"),
  Resolver.jcenterRepo,
  Resolver.bintrayRepo("ticofab", "maven"),
  "Artifactory" at "http://artifactory.mheducation.com/artifactory/libs-release-local/"
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "com.amazonaws" % "aws-java-sdk" % "1.11.313"
).map(_ % "provided")


