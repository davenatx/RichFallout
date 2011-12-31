package com.adi.util

import javax.mail.{Session, Message, Multipart, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage, MimeBodyPart, MimeMultipart}
import javax.activation.{DataSource, FileDataSource, DataHandler}
import java.io.File
/**
 * User: dmp
 * Date: 11/10/11
 * Time: 3:14 PM
 */

class SendReport(val smtpServer: String, val bcc: Boolean) {
  val props = System.getProperties()
  props.put("mail.smtp.host", smtpServer)

  def send(recipients: List[String]){
    // Setup the message
    val message = new MimeMessage(Session.getInstance(props, null))
    message.setFrom(new InternetAddress("support@austindata.com"))

    recipients foreach (x => {
      // add the recipients
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(x))
    })
    message.setSubject("Austin Data - Fallout Report")
	
	if (bcc){
      // BCC Option for QC
      message.addRecipient(Message.RecipientType.BCC, new InternetAddress("dprice@austindata.com"))
      message.addRecipient(Message.RecipientType.BCC, new InternetAddress("wwagner@austindata.com"))
    }
	
    val multipart = new MimeMultipart

    val messageBodyPart = new MimeBodyPart
    messageBodyPart.setText("No Fallout reports were generated today.")
    multipart.addBodyPart(messageBodyPart)

    message.setContent(multipart)
    Transport.send(message)

  }

  def sendReports(recipients: List[String], attachments: List[File]) {
    // Setup the message
    val message = new MimeMessage(Session.getInstance(props, null))
    message.setFrom(new InternetAddress("support@austindata.com"))

    recipients foreach (x => {
      // add the recipients
      message.addRecipient(Message.RecipientType.TO, new InternetAddress(x))
    })
    message.setSubject("Austin Data - Fallout Report")

    if (bcc){
      // BCC Option for QC
      message.addRecipient(Message.RecipientType.BCC, new InternetAddress("dprice@austindata.com"))
      message.addRecipient(Message.RecipientType.BCC, new InternetAddress("wwagner@austindata.com"))
    }

    val multipart = new MimeMultipart

    val messageBodyPart = new MimeBodyPart
    messageBodyPart.setText("Please see the attached Fallout Report(s).")
    multipart.addBodyPart(messageBodyPart)

    attachments foreach (x => {
      val attachmentBodyPart = new MimeBodyPart
      val source = new FileDataSource(x)
      attachmentBodyPart.setDataHandler(new DataHandler(source))
      attachmentBodyPart.setFileName(x.getName)
      multipart.addBodyPart(attachmentBodyPart)
    })

    message.setContent(multipart)
    Transport.send(message)
  }
}