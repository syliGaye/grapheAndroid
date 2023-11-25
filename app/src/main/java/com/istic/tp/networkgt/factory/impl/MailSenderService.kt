package com.istic.tp.networkgt.factory.impl

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.istic.tp.networkgt.factory.IMailSender
import java.io.File
import android.os.Build
import android.os.NetworkOnMainThreadException
import android.os.StrictMode
import androidx.core.content.FileProvider
import java.util.Properties
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.activation.DataSource
import javax.activation.FileDataSource


class MailSenderService(private var context: Context): IMailSender {
    override fun send() {
        TODO("Not yet implemented")
    }

    override fun send(to: String) {
        TODO("Not yet implemented")
    }

    override fun send(attach: File?) {
        if (attach != null){
            try {
                Log.i("Send email", "Mail en cours d'envoi...")
                val TO = arrayOf("")

                // Assurez-vous d'ajuster votre fichier XML dans res/xml pour FileProvider
                val uri = Uri.fromFile(attach)

                val emailIntent = Intent(Intent.ACTION_SEND)
                emailIntent.data = Uri.parse("mailto:")
                emailIntent.type = "text/plain"
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "")
                emailIntent.putExtra(Intent.EXTRA_TEXT, "")
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri)

                // Autorisez le lancement de l'intent en dehors d'une activité
                StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                        .penaltyLog()
                        .build()
                )

                // Vérifiez que l'application de messagerie est disponible avant de démarrer l'activité
                if (emailIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(emailIntent, "Envoyer avec"))
                }
            }
            catch (ex: ActivityNotFoundException){
                Toast.makeText(
                    context,
                    "Une erreur dans le démarrage de l'activité.", Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
            catch (ex: Exception){
                Toast.makeText(
                    context,
                    "Une erreur dans l'envoie de mail.", Toast.LENGTH_SHORT
                ).show()
                ex.printStackTrace()
            }
        }
        else {
            Toast.makeText(
                context,
                "Aucun fichier à envoyer.", Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun send(to: String, text: String) {
        TODO("Not yet implemented")
    }

    override fun send(to: String, attach: File?) {
        if (attach != null){
            /**
            try {
            val uri = Uri.fromFile(attach)
            val properties = Properties()
            properties["mail.smtp.host"] = "smtp.gmail.com" // Remplacez par votre fournisseur de messagerie
            properties["mail.smtp.port"] = "587"
            properties["mail.smtp.auth"] = "true"
            properties["mail.smtp.starttls.enable"] = "true"

            val session = Session.getInstance(properties)

            val message = MimeMessage(session)
            message.setFrom(InternetAddress("brokerphamam@gmail.com")) // Remplacez par votre adresse email
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.subject = "Your subject"
            message.setText("Message to send")

            // Le corps du message
            val multipart = MimeMultipart()
            val bodyPart = MimeBodyPart()
            multipart.addBodyPart(bodyPart)

            // La pièce jointe
            val attachmentPart = MimeBodyPart()
            val fileDataSource: DataSource = FileDataSource(uri.path)
            attachmentPart.dataHandler = javax.activation.DataHandler(fileDataSource)
            attachmentPart.fileName = attach.name // Nom de la pièce jointe
            multipart.addBodyPart(attachmentPart)

            message.setContent(multipart)

            val transport = session.getTransport("smtp")
            transport.connect("brokerphamam@gmail.com", "cgyahhxisjzijkuz") // Remplacez par votre adresse email et mot de passe
            transport.sendMessage(message, message.allRecipients)
            transport.close()
            }
            catch (ex: NetworkOnMainThreadException){
            Toast.makeText(
            context,
            "L'application tente d'accéder au réseau d'une manière qui ne peut pas être effectuée sur le thread principal.",
            Toast.LENGTH_SHORT
            ).show()
            ex.printStackTrace()
            }
            catch (ex: Exception){
            Toast.makeText(
            context,
            "There is no email client installed 1.", Toast.LENGTH_SHORT
            ).show()
            ex.printStackTrace()
            }
             **/
        } else {}
    }
}