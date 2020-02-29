import java.nio.file.Path

import org.scalajs.core.tools.linker.backend.ModuleKind.CommonJSModule

name := "apex-assist"
version := "0.6.0"
scalaVersion := "2.12.3"
parallelExecution in Test := false
//scalaJSOptimizerOptions ~= { _.withDisableOptimizer(true) }
//jsEnv in Test := new org.scalajs.jsenv.nodejs.NodeJSEnv(org.scalajs.jsenv.nodejs.NodeJSEnv.Config().withArgs(List("--inspect-brk")))

unmanagedSourceDirectories in Compile += baseDirectory.value / "vsext/main/scala"

enablePlugins(ScalaJSPlugin)
scalaJSModuleKind := CommonJSModule
scalacOptions += "-P:scalajs:sjsDefinedByDefault"

resolvers += Resolver.sonatypeRepo("public")
libraryDependencies += "io.scalajs" %%% "nodejs" % "0.4.2"
libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.2.8"
libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.9.0"

libraryDependencies += "org.scalatest" %%% "scalatest" % "3.1.0" % "test"

val npmTargetDir = s"target/npm/" // where to generate npm
val npmConf = "npm_config" // directory with static files for NPM package
val npmTask = taskKey[Unit](s"Create npm package in $npmTargetDir")

npmTask := {
  // JS libraries must first be generated
  (Compile / fastOptJS).value
  (Compile / fullOptJS).value

  import java.nio.file.StandardCopyOption.REPLACE_EXISTING
  import java.nio.file.Files.copy
  import java.nio.file.Paths.get

  implicit def toPath (filename: String): Path = get(filename)

  def copyToDir(filePathName:String, dirName:String) = {
    val fileName = new File(filePathName).getName
    copy (s"$filePathName", s"$dirName/$fileName", REPLACE_EXISTING)
  }

  val libName = name.value.toLowerCase()

  val inputDir = "target/scala-2.12"
  val targetDir = s"$npmTargetDir/$libName"
  val sourceDir = "source"
  val distDir = "dist"
  val platformDir = "platform"

  // Create module directory structure
  new File(targetDir).mkdirs()
  List(".vscode", distDir, sourceDir, platformDir)
    .foreach(d => new File(s"$targetDir/$d").mkdirs())

  // copy static files
  copyToDir(s"LICENSE", targetDir)
  copyToDir(s"README.md", targetDir)
  copy("npm/package.json", s"$targetDir/package.json", REPLACE_EXISTING)
  copy("npm/logo.png", s"$targetDir/logo.png", REPLACE_EXISTING)
  copy("npm/.vscodeignore", s"$targetDir/.vscodeignore", REPLACE_EXISTING)
  copy("npm/.vscode/launch.json", s"$targetDir/.vscode/launch.json", REPLACE_EXISTING)
  copy("npm/dist/boot.js", s"$targetDir/dist/boot.js", REPLACE_EXISTING)

  // copy optimized js library
  val fileDist = List(s"$libName-opt.js", s"$libName-opt.js.map")
  for(file <- fileDist) {
    println(s"copy file $inputDir/$file")
    copy(s"$inputDir/$file", s"$targetDir/$distDir/$file", REPLACE_EXISTING)
  }

  // copy non optimized js library (for debug purpose)
  val fileSource = List(s"$libName-fastopt.js", s"$libName-fastopt.js.map")
  for(file <- fileSource) {
    println(s"copy file $inputDir/$file")
    copy(s"$inputDir/$file", s"$targetDir/$sourceDir/$file", REPLACE_EXISTING)
  }

  // copy platform directory
  val platformNamespaces = new File(platformDir).listFiles().map(_.name)
  for(namespace <- platformNamespaces) {
    println(s"copying platform namespace '$namespace'")
    new File(s"$targetDir/platform/$namespace").mkdirs()
    new File(s"$platformDir/$namespace").listFiles().foreach(name =>{
      copy(s"$name", s"$targetDir/$name", REPLACE_EXISTING)
    })
  }

  println(s"NPM package created in $npmTargetDir")
}
