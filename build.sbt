import com.typesafe.sbt.packager.docker.ExecCmd
import sbt.Project.projectToRef

lazy val clients = Seq(scalajsclient)
lazy val scalaV = "2.11.6"

lazy val playserver = (project in file("play")).settings(Seq(
  name := "play",
  scalaVersion := scalaV,
  scalaJSProjects := clients,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "play-scalajs-scripts" % "0.3.0",
    "org.webjars" % "jquery" % "1.11.1"
  ),
  routesGenerator := InjectedRoutesGenerator) ++
  dockerSettings
).enablePlugins(PlayScala).enablePlugins(DockerPlugin).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(sharedJvm, pdf)

lazy val scalajsclient = (project in file("scalajs")).settings(
  name := "scalajs",
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  sourceMapsDirectories += sharedJs.base / "..",
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(sharedJs)

lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSPlay).
  jsSettings(sourceMapsBase := baseDirectory.value / "..")

lazy val pdf = (project in file("pdf")).settings(Seq(
  name := "pdf",
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "com.itextpdf" % "itextpdf" % "5.5.6"
  )
))

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project playserver", _: State)) compose (onLoad in Global).value

// for Eclipse users
//EclipseKeys.skipParents in ThisBuild := false
//
lazy val dockerSettings = Seq(
  dockerExposedPorts := Seq(9000),
  dockerCommands := {
    Seq(dockerCommands.value.head, ExecCmd("RUN", "apt-get", "update"), ExecCmd("RUN", "apt-get", "-y", "install", "ghostscript")) ++ dockerCommands.value.tail
  }
)
