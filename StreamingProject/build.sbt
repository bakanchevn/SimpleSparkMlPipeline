name := "SparkML"

version := "1.0"

scalaVersion := "2.12.10"
lazy val sparkVersion = "3.0.1"
lazy val kafkaVersion = "2.7.0"


mainClass in assembly := Some("github.bakanchevn.MLModelGeneration")


libraryDependencies ++= Seq(
    "com.typesafe"     % "config" % "1.4.0",
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.apache.spark"  % "spark-sql-kafka-0-10_2.12" % sparkVersion,
    "org.apache.kafka"  % "kafka-clients"             % kafkaVersion
)



assemblyMergeStrategy in assembly := {
    case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
    case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
    case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
    case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
    case PathList("org", "apache", xs @ _*) => MergeStrategy.last
    case PathList("com", "google", xs @ _*) => MergeStrategy.last
    case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
    case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
    case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
    case "module-info.class" => MergeStrategy.discard
    case "about.html" => MergeStrategy.rename
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case "plugin.properties" => MergeStrategy.last
    case "log4j.properties" => MergeStrategy.last
    case x => MergeStrategy.first
}
