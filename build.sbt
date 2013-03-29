import AssemblyKeys._

organization := "Austin Data"

name := "Rich Fallout"

version := "1.0"

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-optimize", "-deprecation", "-target:jvm-1.5") 

scalariformSettings

assemblySettings

mainClass in assembly := Some("com.adi.RichFallout")

jarName in assembly <<= (scalaVersion, version) ("rich_fallout" + "_" + _ + "-" + _ + ".jar")

/** Shell */
shellPrompt := { state => "RichFallout" + "> " }

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }


libraryDependencies ++= {
  Seq(
    "net.sf.jt400" 		 % "jt400" 				% "7.6",
    "javax.mail" % "mail" % "1.4" % "compile->default"
  )
}