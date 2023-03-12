import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.{ModuleKind, scalaJSLinkerConfig}

name := "apex-assist"
version := "2.0.1"
scalaVersion := "2.13.10"
scalacOptions += "-deprecation"
Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(ScalaJSPlugin)
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }

libraryDependencies += "net.exoego"               %%% "scala-js-nodejs-v12"                    % "0.12.0"
libraryDependencies += "com.lihaoyi"              %%% "upickle"                                % "1.2.0"
libraryDependencies += "io.github.apex-dev-tools" %%% "apex-ls"                                % "4.1.1"
libraryDependencies += "com.github.nawforce"      %%% "scala-json-rpc"                         % "1.1.0"
libraryDependencies += "com.github.nawforce"      %%% "scala-json-rpc-upickle-json-serializer" % "1.1.0"
libraryDependencies += "org.scala-js"             %%% "scala-js-macrotask-executor"            % "1.1.1" // See library doc for why this is needed
libraryDependencies += "org.scala-js"             %%% "scalajs-java-securerandom"              % "1.0.0" // Needed for scala-json-rpc UUID generation

libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.0" % "test"

Compile / fastOptJS / artifactPath := baseDirectory.value / "dist" / "apex-assist-fastopt.js"
Compile / fullOptJS / artifactPath := baseDirectory.value / "dist" / "apex-assist-opt.js"

val npmTargetDir = s"target/npm/" // where to generate npm
val npmConf      = "npm_config"   // directory with static files for NPM package
val build        = taskKey[Unit](s"Build to JS")

build := {
  // Compile to JS
  (Compile / fastOptJS).value
  (Compile / fullOptJS).value
}
