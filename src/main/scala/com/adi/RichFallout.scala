package com.adi

import com.adi.as400.{FalloutSpoolFileReader}
import com.adi.util.{FalloutPDFReport, SendReport}
import com.ibm.as400.access.{AS400}
import java.io.File
import collection.mutable.Map

/**
 * User: dmp
 * Date: 11/10/11
 * Time: 4:53 PM
 */

object RichFallout {

  type OptionMap = Map[Symbol, Any]

  val usage = """ Usage: [--server address] [--user user] [--password password]
  [--smtpserver address] [--outq outq] [--spoolfileuser user that created spoolfile [--userdata spoolfile user data
  [--email comma seperated email address list] [--bcc ture|false - blind carbon copy for QC] [--markasprocessed:
  true|false - place spool file on hold and change userdata to SENTASPDF """

  def nextOption(parsedArguments: OptionMap, remainingArguments: List[String]): OptionMap = {
    remainingArguments match {
      case Nil => parsedArguments
      case "--server" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("server")-> value), tail)
      case "--user" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("user") -> value), tail)
      case "--password" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("password") -> value), tail)
      case "--smtpserver" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("smtpserver") -> value), tail)
      case "--outq" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("outq") -> value), tail)
      case "--spoolfileuser" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("spoolfileuser") ->
        value), tail)
      case "--userdata" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("userdata") -> value), tail)
      case "--email" :: value :: tail => nextOption(parsedArguments ++ Map(Symbol("email") -> value), tail)
      case "--bcc" :: value :: tail => nextOption(parsedArguments ++Map(Symbol("bcc") -> value.toBoolean), tail)
      case "--markasprocessed" :: value :: tail => nextOption(parsedArguments ++Map(Symbol("markasprocessed") ->
        value.toBoolean), tail)
      case unknownOption :: tail =>
        sys.error("Unknown Option " + unknownOption)
        println(usage)
        sys.exit(1)
    }
  }

  def main(args: Array[String]) = {
   println(usage)

    // Load command line arguments
    val options = nextOption(Map(), args.toList)

    val server = options.getOrElse(Symbol("server"), "localhost").asInstanceOf[String]
    val user = options.getOrElse(Symbol("user"), "").asInstanceOf[String]
    val password = options.getOrElse(Symbol("password"), "").asInstanceOf[String]
    val smtpServer = options.getOrElse(Symbol("smtpserver"), "mail.snan.twtelecom.net").asInstanceOf[String]
    val outq = options.getOrElse(Symbol("outq"), "qprint").asInstanceOf[String]
    val spoolfileUser = options.getOrElse(Symbol("spoolfileuser"), "T2FALLOUT").asInstanceOf[String]
    val userdata = options.getOrElse(Symbol("userdata"), "TM4362CR").asInstanceOf[String]
    val email = options.getOrElse(Symbol("email"), "").asInstanceOf[String]
    val bcc = options.getOrElse(Symbol("bcc"), true).asInstanceOf[Boolean]
    val markasprocessed = options.getOrElse(Symbol("markasprocessed"), false).asInstanceOf[Boolean]

    val recipList = email.split(",").toList

    val as400 = new AS400(server,user,password)
    try{
      val spoolFileReader = new FalloutSpoolFileReader(as400, spoolfileUser, userdata, outq, markasprocessed)
      val spoolFiles = spoolFileReader.getSpoolFiles

      // Variable
      var attachList = List[File]()

      spoolFiles foreach(x => {
        // Get the entire company field of the first page of the report and pattern match to get company code
        val comp = x.pages.head.substring(28,71).trim match {
          case "Gracy Title Company" => "GR"
          case "Prosperity Title" => "PR"
          case "Advantage Title" => "AV"
          case "Stewart Title" => "SW"
          case "Graystone Title" => "GS"
          case _ => "COMP"
        }
        // Get the report date
        val date = x.pages.head.substring(124,134).replace("/","").trim
        val pdfReport = new FalloutPDFReport
        val file = new File(comp + "-" + date + ".pdf")
        attachList ::= file
        pdfReport.open(file.getName)
        pdfReport.createReport(x.pages)
      })

      val sendReport = new SendReport(smtpServer, bcc)
      // determine which e-mail to send
      attachList.isEmpty match {
        case false => {
          sendReport.sendReports(recipList, attachList)
          // delete the PDF files
          attachList foreach (x => {
            x.delete
          })
        }
        case true => sendReport.send(recipList)
      }
    }finally{
      as400.disconnectAllServices
    }
  }

}