import sbt.io.Path.relativeTo

import java.text.SimpleDateFormat

name := "xauth-api"
organization := "io.xauth"
organizationName := "XAuth"
maintainer := "Enrico Russo <enrico.russo.84@gmail.com>"

version := "2.1.3"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    Universal / mappings ++= {
      val publicDir = baseDirectory.value / "public"
      if (publicDir.exists())
        (publicDir ** "*").pair(relativeTo(baseDirectory.value))
      else
        Seq()
    }
  )

scalaVersion := "2.12.6"

scalacOptions ++= Seq(
  "-feature", "-language:postfixOps"
)

// Json-Schema validator todo: unmanaged dependency
//resolvers += ("emueller-bintray" at "http://dl.bintray.com/emueller/maven")
resolvers += "jitpack".at("https://jitpack.io")

libraryDependencies ++= Seq(
  guice,

  specs2 % Test,

  // Http client
  ws,

  filters,

  // Play
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,

  // Play Mailer
  "com.typesafe.play" %% "play-mailer" % "6.0.1",

  // MongoDb
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.7.1",
  "org.reactivemongo" %% "play2-reactivemongo" % "1.1.0-play28-RC6",

  // Json-Schema validator todo: unmanaged dependency
  //"com.eclipsesource" %% "play-json-schema-validator" % "0.9.5",
  "io.circe" %% "circe-json-schema" % "0.2.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",

  // Akka
  "com.typesafe.akka" %% "akka-distributed-data" % "2.6.19",

  // JWT
  "com.pauldijou" %% "jwt-core" % "0.17.0",

  // JWK
  "com.nimbusds" % "nimbus-jose-jwt" % "7.0.1",

  // Scrypt implementation for password encryption
  "com.lambdaworks" % "scrypt" % "1.4.0",

  // SOAP client
  "com.sandinh" %% "play-soap" % "1.8.0",

  // xenum-scala
  "it.russoft.xenum" %% "xenum-scala" % "1.3.1",

  // Cats
  "org.typelevel" %% "cats-core" % "2.7.0"
)

// disabling documentation generation and publish
//sources in(Compile, doc) := Seq.empty
//publishArtifact in(Compile, packageDoc) := false

// setting integration test source directory
//scalaSource in Test := baseDirectory.value / "test-integration"

Compile / sourceGenerators += Def.task {
  import java.util.Date
  val file = (Compile / sourceManaged).value / "/io/xauth/generated/ApplicationInfo.scala"
  val contents = IO.read(new File(s"${baseDirectory.value}/application-info.scala.template"))
    .replace("{{NAME}}", name.value)
    .replace("{{VERSION}}", version.value)
    .replace("{{BUILT_AT}}", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'").format(new Date))
  IO.write(file, contents)
  Seq(file)
}.taskValue

lazy val dockerComposeUp = taskKey[Unit]("starting docker services")
lazy val dockerComposeDown = taskKey[Unit]("stopping docker services")

val containerPrefix = "xauth"

dockerComposeUp := {
  import java.lang.Thread.sleep
  import scala.sys.process.Process

  val env = "-test"
  val composeFile = f"${baseDirectory.value}/docker/compose/docker-compose$env.yml"

  Process("docker-compose" :: "-f" :: composeFile :: "up" :: Nil).run

  def upCheck(): Boolean = {
    println("waiting for services up...")
    sleep(3000L)
    val up = Process("docker-compose" :: "-f" :: composeFile :: "ps" :: Nil)
      .lineStream.filter(_.startsWith(containerPrefix)).forall(_.matches(".*\\s+Up\\s+.*"))
    if (up) true else upCheck()
  }

  upCheck()
}

dockerComposeDown := {
  import scala.sys.process.Process

  val env = "-test"
  val composeFile = f"${baseDirectory.value}/docker/compose/docker-compose$env.yml"

  Process("docker-compose" :: "-f" :: composeFile :: "down" :: Nil).!
}

// Making Docker image
lazy val install =
  taskKey[Unit]("Building and pushing docker image")

install := {
  scala.sys.process.Process(
    f"${baseDirectory.value}/docker/build/build.sh" ::
      "-d" :: f"${baseDirectory.value}" ::
      "-a" :: f"${name.value}" ::
      "-v" :: f"${version.value}" ::
      "-e" :: sys.props.getOrElse("environment", "development") ::
      "-r" :: "registry.xauth.io" ::
      "-u" :: "xauth" :: Nil
  ).!
}
