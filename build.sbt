// Name of the project and base package name
name := "sqs-blog"
organization := "com.mhedu"

// Add Scala and Spark Version
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

// Spark "provided" plugins
libraryDependencies ++= Seq(
  // Core Spark Libs
  "org.eclipse.jetty" % "jetty-servlet" % "9.3.20.v20170531",
  // AWS SDK
  "com.amazonaws" % "aws-java-sdk" % "1.11.313",
  "io.delta" %% "delta-core" % "0.7.0"
).map(_ % "provided")

// Libraries Dependencies
libraryDependencies ++= Seq(
  "io.searchbox" % "jest" % "5.3.2",
  "io.searchbox" % "jest-common" % "5.3.2",
  "io.ticofab" %% "aws-request-signer" % "0.5.0",
  "org.scalaj" % "scalaj-http_2.12" % "2.4.2",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "org.skinny-framework" %% "skinny-validator" % "3.1.0",
  "org.skinny-framework" %% "skinny-common" % "3.1.0",
  "joda-time" % "joda-time" % "2.10",
  "org.joda" % "joda-convert" % "2.1.1",
  "org.apache.commons" % "commons-dbcp2" % "2.1.1",
  "com.databricks" %% "dbutils-api" % "0.0.4",
  "org.apache.commons" % "commons-dbcp2" % "2.1.1",
  "com.newrelic.agent.java" % "newrelic-api" % "5.13.0",
  "com.newrelic.telemetry" % "telemetry-core" % "0.6.0",
  "com.newrelic.telemetry" % "telemetry-http-okhttp" % "0.6.0",
  "com.newrelic.telemetry" % "telemetry" % "0.6.0"
)

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0"

// This test library does not support parallel execution.
// Configure default parameters to scalatest:
// http://www.scalatest.org/user_guide/using_scalatest_with_sbt
parallelExecution in ThisBuild := false
parallelExecution in Test := false
parallelExecution in IntegrationTest := false
fork in Test := true
logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

