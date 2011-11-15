import sbt._


class RichFallout(info: ProjectInfo) extends DefaultProject(info) {

  /**
   * Application dependencies
   */
  val iText = "com.lowagie" % "itext" % "2.1.7" % "compile->default" withJavadoc
  val jt400 = "net.sf.jt400" % "jt400" % "6.7" % "compile->default"
  val javaMail = "javax.mail" % "mail" % "1.4" % "compile->default"


  /**
   * Manifest
   */
  override def mainClass = Some("com.adi.RichFallout")

  override def manifestClassPath = {
    val dependentJarNames = dependentJars.getFiles.map(_.getName).filter(_.endsWith(".jar"))
    Some(dependentJarNames.map { "lib/" + _ }.mkString(" "))
  }

  val libraryJarPath = outputPath / "lib"

  val dependentJars = mainDependencies.libraries +++ mainDependencies.scalaJars

  def collectJarsTask = {
    FileUtilities.copyFlat(dependentJars.get, libraryJarPath, log)
  }

  lazy val collectJars = task { collectJarsTask; None } dependsOn(compile)
}
