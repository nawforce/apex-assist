import java.nio.file.{Files, Path}
import scala.collection.JavaConverters._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{ModuleKind, scalaJSLinkerConfig}

name := "apex-assist"
version := "1.1.0"
scalaVersion := "2.13.3"
scalacOptions += "-deprecation"
parallelExecution in Test := false

enablePlugins(ScalaJSPlugin)
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

resolvers += Resolver.sonatypeRepo("public")
libraryDependencies += "net.exoego" %%% "scala-js-nodejs-v12" % "0.12.0"
libraryDependencies += "com.lihaoyi" %%% "upickle" % "1.2.0"
libraryDependencies += "com.github.nawforce" %%% "pkgforce" % "1.3.2"
libraryDependencies += "com.github.nawforce" %%% "scala-json-rpc" % "1.0.1"
libraryDependencies += "com.github.nawforce" %%% "scala-json-rpc-upickle-json-serializer" % "1.0.1"

libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.0" % "test"

val npmTargetDir = s"target/npm/" // where to generate npm
val npmConf = "npm_config" // directory with static files for NPM package
val createPackage = taskKey[Unit](s"Create npm package in $npmTargetDir")

createPackage := {
  // JS libraries must first be generated
  (Compile / fastOptJS).value
  (Compile / fullOptJS).value

  import java.nio.file.Files.copy
  import java.nio.file.Paths.get
  import java.nio.file.StandardCopyOption.REPLACE_EXISTING

  implicit def toPath(filename: String): Path = get(filename)

  def copyToDir(filePathName: String, dirName: String) = {
    val fileName = new File(filePathName).getName
    copy(s"$filePathName", s"$dirName/$fileName", REPLACE_EXISTING)
  }

  def copyDir(src: Path, dest: Path): Unit = {
    Files.walk(src).iterator().asScala.foreach(sourceFile => {
      if (Files.isRegularFile(sourceFile)) {
        val destFile = dest.resolve(src.relativize(sourceFile))
        destFile.getParent.toFile.mkdirs()
        copy(sourceFile, dest.resolve(src.relativize(sourceFile)), REPLACE_EXISTING)
      }
    })
  }

  val libName = name.value.toLowerCase()

  val inputDir = "target/scala-2.13"
  val targetDir = s"$npmTargetDir/$libName"
  val sourceDir = "source"
  val distDir = "dist"
  val jarsDir = "jars"
  val grammarsDir = "grammars"
  val webviewDir = "webview"

  // Create module directory structure
  new File(targetDir).mkdirs()
  List(".vscode", distDir, sourceDir, jarsDir, grammarsDir, webviewDir)
    .foreach(d => new File(s"$targetDir/$d").mkdirs())

  // copy static files
  copyToDir(s"LICENSE", targetDir)
  copy("npm/package.json", s"$targetDir/package.json", REPLACE_EXISTING)
  copy("npm/README.md", s"$targetDir/README.md", REPLACE_EXISTING)
  copy("npm/CHANGELOG.md", s"$targetDir/CHANGELOG.md", REPLACE_EXISTING)
  copy("npm/logo.png", s"$targetDir/logo.png", REPLACE_EXISTING)
  copy("npm/.vscodeignore", s"$targetDir/.vscodeignore", REPLACE_EXISTING)
  copy("npm/.vscode/launch.json", s"$targetDir/.vscode/launch.json", REPLACE_EXISTING)
  copy("npm/dist/boot.js", s"$targetDir/dist/boot.js", REPLACE_EXISTING)
  copy("npm/grammars/apex.tmLanguage", s"$targetDir/grammars/apex.tmLanguage", REPLACE_EXISTING)

  // copy optimized js library
  val fileDist = List(s"$libName-opt.js"/*, s"$libName-opt.js.map" */)
  for (file <- fileDist) {
    println(s"copy file $inputDir/$file")
    copy(s"$inputDir/$file", s"$targetDir/$distDir/$file", REPLACE_EXISTING)
  }

  // copy non optimized js library (for debug purpose)
  /*
  val fileSource = List(s"$libName-fastopt.js", s"$libName-fastopt.js.map")
  for (file <- fileSource) {
    println(s"copy file $inputDir/$file")
    copy(s"$inputDir/$file", s"$targetDir/$distDir/$file", REPLACE_EXISTING)
  }*/

  // copy jars directory
  val jarFiles = new File("npm/jars").listFiles().map(_.name)
  for (jarFile <- jarFiles) {
    println(s"copy file $jarFile")
    copy(s"npm/jars/$jarFile", s"$targetDir/$jarsDir/$jarFile", REPLACE_EXISTING)
  }

  // copy webview directory
  copyDir("npm/webview/build", s"$targetDir/$webviewDir")


  println(s"NPM package created in $npmTargetDir")
}
