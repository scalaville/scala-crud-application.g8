import Dependencies._

ThisBuild / organization := "io.scalaville"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file(".")).settings(
  name := "$name$",
  resolvers += "Confluent Maven Repository" at "https://packages.confluent.io/maven/"
)

lazy val loggingDependencies = Seq(
  "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.5",
  "ch.qos.logback"              % "logback-classic"          % "1.4.4",
  "org.typelevel"              %% "log4cats-slf4j"           % "2.5.0",
  "io.laserdisc"               %% "log-effect-fs2"           % logEffectVersion,
  "org.codehaus.janino"         % "janino"                   % "3.1.9" % Runtime,
  "net.logstash.logback"        % "logstash-logback-encoder" % "7.2"   % Runtime
)

lazy val http4s    = "org.http4s"              %% "http4s-ember-server" % http4sVersion
lazy val smlCommon = "com.softwaremill.common" %% "tagging"             % smlCommonVersion
lazy val swaggerUI =
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % swaggerUIVersion

lazy val tapir = Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-core",
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server",
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe",
  "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server",
  "com.softwaremill.sttp.tapir" %% "tapir-cats"
).map(_ % tapirVersion)

lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val kafka = Seq(
  "org.apache.kafka"  % "kafka-clients",
  "org.apache.kafka"  % "kafka-streams",
  "org.apache.kafka" %% "kafka-streams-scala"
).map(_ % kafkaVersion)

lazy val cats = Seq(
  "org.typelevel" %% "cats-core"   % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion
)

lazy val avro = Seq(
  "io.confluent" % "kafka-avro-serializer",
  "io.confluent" % "kafka-streams-avro-serde"
).map(_ % avroVersion)

lazy val fs2 = Seq(
  "co.fs2" %% "fs2-core",
  "co.fs2" %% "fs2-io"
).map(_ % fs2Version)

lazy val avro4s = Seq(
  "com.sksamuel.avro4s" % "avro4s-core_2.13",
  "com.sksamuel.avro4s" % "avro4s-kafka_2.13"
).map(_ % avro4sVersion)

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core",
  "org.tpolecat" %% "doobie-hikari", // HikariCP transactor
  "org.tpolecat" %% "doobie-postgres"
).map(_ % doobieVersion)

lazy val flyway = "org.flywaydb" % "flyway-core" % flywayVersion

lazy val pureConfig =
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion

val prometheus = Seq(
  "io.prometheus" % "simpleclient",
  "io.prometheus" % "simpleclient_hotspot"
).map(_ % prometheusVersion)

val micrometer = Seq(
  "io.micrometer" % "micrometer-registry-prometheus" % micrometerVersion,
  "io.micrometer" % "micrometer-core"                % micrometerVersion
)

libraryDependencies ++=
  loggingDependencies ++ kafka ++ circe ++ tapir ++
    cats ++ fs2 ++ avro ++ doobie ++ prometheus ++
    micrometer :+ flyway :+ http4s :+ smlCommon :+ swaggerUI :+ pureConfig
