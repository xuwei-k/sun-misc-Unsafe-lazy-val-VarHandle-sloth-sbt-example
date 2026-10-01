scalaVersion := "3.9.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.scalameta" %% "munit" % "1.3.6" % Test,
)

Test / fork := true

run / fork := true

Test / javaOptions += "--sun-misc-unsafe-memory-access=deny"

run / javaOptions += "--sun-misc-unsafe-memory-access=deny"
