package com.adi.as400

import java.util.Enumeration
import com.ibm.as400.access._
import com.ibm.as400.access.PrintObjectList._
import collection.JavaConversions._
import com.adi.util.SendReport

/**
 * User: dmp
 * Date: 10/31/11
 * Time: 4:03 PM
 */

/**
 * container for pages of a spool file
 */
case class SpoolFile(pages: List[String])

/**
 * Static values for SpoolFileReader class
 */
object FalloutSpoolFileReader {
  // PrintParamList to use for Transform
  val transformPrintParam = {
    val param = new PrintParameterList();
    param.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");
    param.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT, "/QSYS.LIB/QWPDEFAULT.WSCST")
    param
  }
  // PrintParamList to set User Data to indicate spooled file was processed
  val afterProcessingPrintParam = {
    val param = new PrintParameterList();
    param.setParameter(PrintObject.ATTR_USERDATA, "SENTASPDF")
    param
  }
}

/**
 * Reads fallout spooled files filtered by the user, outq, and user data.
 */
class FalloutSpoolFileReader(val as400: AS400, val spoolfileUser: String, val userdata: String, val outq: String,
    val markasprocessed: Boolean) {

  def getSpoolFiles: List[SpoolFile] = {
    // Create list of spooled files
    val splfList = new SpooledFileList(as400)
    try {
      splfList.setUserFilter(spoolfileUser)
      splfList.setUserDataFilter(userdata)
      splfList.setQueueFilter("/QSYS.LIB/QUSRSYS.LIB/" + outq + ".OUTQ")
      splfList.openSynchronously
      splfList.waitForListToComplete

      // Use collection.JavaConversions.enumerationAsScalaIterator to cast untyped Java Enumeration from JT400 to
      // Scala Iterator[SpooledFile]
      val splFiles: Iterator[SpooledFile] = splfList.getObjects.asInstanceOf[java.util.Enumeration[SpooledFile]]

      // Return list of spool file entries
      splFiles.map(spooledFile => readSpooledFile(spooledFile)).toList

    } finally {
      splfList.close
    }

  }

  // Function to transform contents of spooled file to SpoolFile object
  val readSpooledFile: SpooledFile => SpoolFile = spooledFile => {
    val inputStream = spooledFile.getTransformedInputStream(FalloutSpoolFileReader.transformPrintParam)
    val buffer = new Array[Byte](1024)
    val builder = new StringBuilder()
    Stream.continually(inputStream.read(buffer)).takeWhile(_ != -1).foreach(x => {
      builder.append(new String(buffer, 0, x))
    })

    if (markasprocessed) {
      // Place spooled file on hold and set user data to indicate it has been processed
      spooledFile.hold("*IMMED")
      spooledFile.setAttributes(FalloutSpoolFileReader.afterProcessingPrintParam)
    }

    // Split the builder on the form feed character and create list of pages for SpoolFile constructor
    SpoolFile(builder.toString.split("\f").toList)
  }
}

