package es.iesjandula.reaktor.notifications_server.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario.*;
import es.iesjandula.reaktor.notifications_server.repository.*;

@Service
public class EmailService
{
    @Autowired 
    private Credential gmailCredentials;

    @Autowired 
    private INotificacionesEmailUsuarioRepository notificacionEmailUsuarioRepository;

    @Autowired 
    private INotificacionesEmailParaUsuarioRepository paraUsuarioRepository;

    @Autowired 
    private INotificacionesEmailCopiaUsuarioRepository copiaUsuarioRepository;

    @Autowired 
    private INotificacionesEmailCopiaOcultaUsuarioRepository copiaOcultaUsuarioRepository;

    @Autowired 
    private IUsuarioRepository usuarioRepository;

    /**
     * Envía un email y guarda la notificación en la base de datos.
     */
    public String sendEmail(
            String from,
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String subject,
            String bodyText
    ) throws MessagingException, IOException, Exception
    {
        // ======= ENVÍO DEL EMAIL =======
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                this.gmailCredentials)
                .setApplicationName("Reaktor-FirebaseServer")
                .build();

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));

        if (to != null)
            for (String recipient : to)
                email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
        if (cc != null)
            for (String recipient : cc)
                email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(recipient));
        if (bcc != null)
            for (String recipient : bcc)
                email.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(recipient));

        email.setSubject(subject);
        email.setText(bodyText);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessageBytes);

        Message message = new Message();
        message.setRaw(encodedEmail);

        Message sentMessage = service.users().messages().send("me", message).execute();

        // ======= GUARDAR EN BBDD =======
        NotificacionEmailUsuario notificacion = new NotificacionEmailUsuario();
        notificacion.setAsunto(subject);
        notificacion.setContenido(bodyText);
        notificacion.setFechaCreacion(new Date());

        // Usuario emisor
        Usuario usuarioEmisor = getOrCreateUsuario(from);
        notificacion.setUsuario(usuarioEmisor);

        // Guardar notificación principal
        NotificacionEmailUsuario saved = this.notificacionEmailUsuarioRepository.save(notificacion);

        // Guardar destinatarios (TO)
        if (to != null) 
        {
            for (String correo : to) 
            {
                Usuario u = getOrCreateUsuario(correo);
                NotificacionEmailParaUsuario nep = new NotificacionEmailParaUsuario();
                nep.setUsuario(u);
                nep.setNotificacionEmailUsuario(saved);
                this.paraUsuarioRepository.save(nep);
            }
        }

        // Guardar CC
        if (cc != null) 
        {
            for (String correo : cc) 
            {
                Usuario u = getOrCreateUsuario(correo);
                NotificacionEmailCopiaUsuario nec = new NotificacionEmailCopiaUsuario();
                nec.setUsuario(u);
                nec.setNotificacionEmailUsuario(saved);
                this.copiaUsuarioRepository.save(nec);
            }
        }

        // Guardar BCC
        if (bcc != null) 
        {
            for (String correo : bcc) 
            {
                Usuario u = getOrCreateUsuario(correo);
                NotificacionEmailCopiaOcultaUsuario neco = new NotificacionEmailCopiaOcultaUsuario();
                neco.setUsuario(u);
                neco.setNotificacionEmailUsuario(saved);
                this.copiaOcultaUsuarioRepository.save(neco);
            }
        }

        return sentMessage.toPrettyString();
    }

    /**
     * Busca o crea un usuario por email.
     */
    private Usuario getOrCreateUsuario(String email)
    {
        return this.usuarioRepository.findByEmail(email)
                .orElseGet(() -> 
                {
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    return this.usuarioRepository.save(nuevo);
                });
    }
}
