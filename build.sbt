scalaVersion := "3.9.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.13.0",
  "org.scalameta" %% "munit" % "1.3.6" % Test,
)

Test / fork := true

run / fork := true

Test / javaOptions += "--sun-misc-unsafe-memory-access=deny"

run / javaOptions += "--sun-misc-unsafe-memory-access=deny"

@transient
val slothDirectory = settingKey[File]("")

Seq(Compile, Test, Runtime).flatMap{ x =>
  Seq[Def.Setting[?]](
    (x / slothDirectory) := target.value / "sloth" / Defaults.nameForSrc(x.name),
    cleanFiles += (x / slothDirectory).value,
    (x / externalDependencyClasspath) := Def.uncached {
      // TODO
      //  - 毎回実行したらすごく無駄なので、いい感じにcacheする
      //  - Configration毎に重複して実行してるのも無駄
      //  - externalDependencyClasspath を直接上書きするので最適なのか？も謎

      val converter = fileConverter.value
      val log = streams.value.log
      scribe.Logger.root
        .clearHandlers()
        .withHandler(
          minimumLevel = Some(scribe.Level.Warn),
        )
        .replace()

      val originalClasspath = (x / externalDependencyClasspath).value

      assert(
        originalClasspath.map(_.data).map(converter.toPath).distinctBy(_.toFile.getName).size == originalClasspath.size,
        "duplicate jar name"
      )

      originalClasspath.map {
        f =>
          val originalJarFile = converter.toPath(f.data)
          val jarName = originalJarFile.toFile.getName
          val newJarFile = (x / slothDirectory).value / jarName
          IO.delete(newJarFile)
          IO.touch(newJarFile)
          val result = sloth.jar.JarProcessor.process(
            originalJarFile,
            newJarFile.toPath,
            originalClasspath.map(_.data).map(converter.toPath)
          )
          def info(s: String) = {
            log.info(s"[${name.value}/${Defaults.nameForSrc(x.name)}/${jarName}] $s")
          }
          info(s"sloth totalClasses = ${result.totalClasses}")
          info(s"sloth patchedClasses = ${result.patchedClasses}")

          // TODO classpathうまく設定しないとエラー出る場合がある
//          assert(result.errors.isEmpty, result)
//          assert(result.failedClasses == 0, result)
          Attributed(converter.toVirtualFile(newJarFile.toPath))(f.metadata)
      }
    }
  )
}
