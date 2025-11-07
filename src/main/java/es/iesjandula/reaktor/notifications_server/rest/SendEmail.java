package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.EmailRequestDto;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import es.iesjandula.reaktor.notifications_server.models.Usuario;
import es.iesjandula.reaktor.notifications_server.models.notificacion_emails.usuario.*;
import es.iesjandula.reaktor.notifications_server.repository.*;

@Slf4j
@RestController
public class SendEmail
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

    @RequestMapping(method = RequestMethod.POST, value = "/")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequestDto emailRequestDto)
    {
        try
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
            email.setFrom(new InternetAddress(emailRequestDto.getFrom()));

            if (emailRequestDto.getTo() != null)
                for (String recipient : emailRequestDto.getTo())
                    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
            if (emailRequestDto.getCc() != null)
                for (String recipient : emailRequestDto.getCc())
                    email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(recipient));
            if (emailRequestDto.getBcc() != null)
                for (String recipient : emailRequestDto.getBcc())
                    email.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(recipient));

            email.setSubject(emailRequestDto.getSubject());
            email.setText(emailRequestDto.getBody());

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessageBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);
            Message sentMessage = service.users().messages().send("me", message).execute();

            // ======= GUARDAR EN BBDD =======
            NotificacionEmailUsuario notificacion = new NotificacionEmailUsuario();
            notificacion.setAsunto(emailRequestDto.getSubject());
            notificacion.setContenido(emailRequestDto.getBody());
            notificacion.setFechaCreacion(new Date());

            Usuario usuarioEmisor = getOrCreateUsuario(emailRequestDto.getFrom());
            notificacion.setUsuario(usuarioEmisor);

            NotificacionEmailUsuario saved = this.notificacionEmailUsuarioRepository.save(notificacion);

            if (emailRequestDto.getTo() != null)
            {
                for (String correo : emailRequestDto.getTo())
                {
                    Usuario u = getOrCreateUsuario(correo);
                    NotificacionEmailParaUsuario nep = new NotificacionEmailParaUsuario();
                    nep.setUsuario(u);
                    nep.setNotificacionEmailUsuario(saved);
                    this.paraUsuarioRepository.save(nep);
                }
            }

            if (emailRequestDto.getCc() != null)
            {
                for (String correo : emailRequestDto.getCc())
                {
                    Usuario u = getOrCreateUsuario(correo);
                    NotificacionEmailCopiaUsuario nec = new NotificacionEmailCopiaUsuario();
                    nec.setUsuario(u);
                    nec.setNotificacionEmailUsuario(saved);
                    this.copiaUsuarioRepository.save(nec);
                }
            }

            if (emailRequestDto.getBcc() != null)
            {
                for (String correo : emailRequestDto.getBcc())
                {
                    Usuario u = getOrCreateUsuario(correo);
                    NotificacionEmailCopiaOcultaUsuario neco = new NotificacionEmailCopiaOcultaUsuario();
                    neco.setUsuario(u);
                    neco.setNotificacionEmailUsuario(saved);
                    this.copiaOcultaUsuarioRepository.save(neco);
                }
            }

            log.info("Correo enviado con éxito");
            return ResponseEntity.ok(sentMessage.toPrettyString());
        }
        catch (MessagingException | IOException exception)
        {
            String errorMessage = "Error al enviar el email por Gmail API";
            log.error(errorMessage, exception);
            NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
        catch (Exception exception)
        {
            String errorMessage = "Error inesperado al enviar el correo electrónico";
            log.error(errorMessage, exception);
            NotificationsServerException notificationsServerException = new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
            return ResponseEntity.status(500).body(notificationsServerException.getBodyExceptionMessage());
        }
    }

    private Usuario getOrCreateUsuario(String email)
    {
        return this.usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setEmail(email);
                    return this.usuarioRepository.save(nuevo);
                });
    }
}
