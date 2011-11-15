package com.adi.util

import com.lowagie.text.{Document, Paragraph, PageSize, Font, FontFactory}
import com.lowagie.text.pdf.{PdfWriter, BaseFont}


/**
 * User: dmp
 * Date: 11/9/11
 * Time: 7:00 PM
 */

object FalloutPDFReport {
  // Load the font path from the resources directory in development or the Jar in production
  val fontFilePath = {
    var fontPath = ""
    val fontURIPath = this.getClass().getClassLoader.getResource("lucon.ttf").toURI
    // Production where path is to jar containing a !
    if (fontURIPath.toString.contains("!")){
      val pos = fontURIPath.toString.indexOf("!") + 1
      fontPath = fontURIPath.toString.substring(pos)
    }
    // Development on the file system
    else{
      fontPath = fontURIPath.getPath
    }
    fontPath
  }
  // Owner password to encrypt the PDF and only allow printing
  val ownerPassword = {
    "AUSTINDATA505470".getBytes
  }
}

class FalloutPDFReport {
  private val document = new Document(PageSize.LETTER)

  def open(name: String) {
    val writer = PdfWriter.getInstance(document, new java.io.FileOutputStream(name))
    /*
    // Encrypt the PDF using an owner password and only allow printing
    writer.setEncryption(null, FalloutPDFReport.ownerPassword, PdfWriter.ALLOW_PRINTING,
      PdfWriter.STANDARD_ENCRYPTION_128)
    writer.createXmpMetadata()
    */
    document.addAuthor("Austin Data")
    document.addCreator("RichFallout")
    document.setMargins(18f, 18f, 1f, 1f)
    document.open()
  }

  def createReport(pages: List[String]){

    val font = new Font(BaseFont.createFont(FalloutPDFReport.fontFilePath, BaseFont.WINANSI, BaseFont.EMBEDDED),10)

    pages.foreach( x => {
      val p = new Paragraph()
      p.setFont(font)
      p.setLeading(10f) //Sets the line spacing
      p.add(x)
      document.add(p)
      document.newPage
    })
    document.close
  }

}