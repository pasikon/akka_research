name := "mirror"
organization := "com.example.akka-stream-kafka"
scalaVersion := "2.12.1"

libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.14"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.2" % Runtime
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// (optional) If you need scalapb/scalapb.proto or anything from
// google/protobuf/*.proto


enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerBaseImage := "java:openjdk-8-jre"
dockerRepository := Some("docker-hub-username")
