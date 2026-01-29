package es.iesjandula.reaktor.notifications_server.services;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import es.iesjandula.reaktor.base_client.dtos.NotificationEmailDto;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnvioCorreosGmailService
{
    /* Atributo - Credenciales de Gmail */
    @Autowired(required = false)
    private Credential gmailCredentials;

    /* Atributo - Nombre de la aplicación de Gmail */
    @Value("${reaktor.gmail.appName}")
    private String gmailAppName ;

    /* Atributo - Remitente del email */
    @Value("${reaktor.gmail.from}")
    private String from;

    /**
     * Método - Enviar correo a través de Gmail API
     *
     * @param notificationEmailDto - La petición de email a enviar
     * @throws NotificationsServerException - Si hay un error al enviar el correo electrónico
     */
    public void enviarCorreoGmailAPI(NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Configuro el servicio de Gmail
            Gmail service = this.crearServicioGmail();

            // Configuro el mensaje de email
            MimeMessage email = this.crearMensajeEmail();

            // Añado el remitente, subject y contenido del email
            this.enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(email, notificationEmailDto);

            // Configuro los destinatarios del email
            this.enviarCorreoGmailAPIConfigurarDestinatarios(email, notificationEmailDto);

            // Configuro el envío del email y obtengo el mensaje final a enviar
            Message message = this.configurarEnvio(email, notificationEmailDto);

            // Envío el email mediante Gmail API
            service.users().messages().send("me", message).execute();

            log.info("Email enviado correctamente mediante Gmail API");
        }
        catch (NotificationsServerException exception)
        {
            // Propago la excepción tal cual si ya es controlada
            throw exception;
        }
        catch (Exception exception)
        {
            // Error inesperado al enviar el correo
            String errorMessage = "Error al enviar el email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }


    /** 
     * Método - Crear servicio de Gmail
     *
     * @return Gmail - El servicio de Gmail creado
     * @throws NotificationsServerException - Si hay un error al crear el servicio de Gmail
     */
    private Gmail crearServicioGmail() throws NotificationsServerException
    {
        try
        { 
            // Verificar que las credenciales estén disponibles
            if (this.gmailCredentials == null)
            {
                String errorMessage = "Las credenciales de Gmail no están configuradas. " +
                                      "Por favor, configure el token de autorización en la carpeta 'tokens'.";

                log.error(errorMessage);
                throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage);
            }
            
            // Creo el servicio de Gmail
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Creo el servicio de Gmail
            return new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), this.gmailCredentials)
                            .setApplicationName(this.gmailAppName)
                            .build() ;
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al crear el servicio de Gmail";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
        
    }

    /** Método - Crear mensaje de email
     *
     * @return MimeMessage - El mensaje de email creado
     */
    private MimeMessage crearMensajeEmail()
    {
        // Añado las propiedades de la sesión
        Properties properties = new Properties();

        // Creo la sesión
        Session session = Session.getDefaultInstance(properties, null);

        // Devuelvo el mensaje de email
        return new MimeMessage(session);
    }

    /** Método - Configurar remitente, subject y contenido del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar el remitente, subject y contenido del email
     */
    private void enviarCorreoGmailAPIConfigurarRemitenteSubjectContenido(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Añado el remitente del email
            email.setFrom(new InternetAddress(this.from));

            // Añado el asunto y el cuerpo del email
            // Fuerza UTF-8 y contenido HTML (si no, el cliente lo muestra como texto plano con etiquetas)
            email.setSubject(notificationEmailDto.getSubject(), "UTF-8");
            email.setContent(notificationEmailDto.getBody(), "text/html; charset=UTF-8");
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar el remitente, subject y contenido del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /** Método - Configurar destinatarios del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @throws NotificationsServerException - Si hay un error al configurar los destinatarios del email
     */
    private void enviarCorreoGmailAPIConfigurarDestinatarios(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Añado los destinatarios del email
            if (notificationEmailDto.getTo() != null)
            {
                for (String recipient : notificationEmailDto.getTo())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
                }
            }

            // Añado los copias del email
            if (notificationEmailDto.getCc() != null)
            {
                for (String recipient : notificationEmailDto.getCc())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(recipient));
                }
            }

            // Añado los copias ocultas del email
            if (notificationEmailDto.getBcc() != null)
            {
                for (String recipient : notificationEmailDto.getBcc())
                {
                    email.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(recipient));
                }
            }
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar los destinatarios del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }

    /** Método - Configurar envío del email
     *
     * @param email - El mensaje de email a configurar
     * @param notificationEmailDto - La petición de email a configurar
     * @return Message - El mensaje de email configurado
     * @throws NotificationsServerException - Si hay un error al configurar el envío del email
     */
    private Message configurarEnvio(MimeMessage email, NotificationEmailDto notificationEmailDto) throws NotificationsServerException
    {
        try
        {
            // Convierto el email a un array de bytes
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            email.writeTo(buffer);

            // Convierto el array de bytes a una cadena de base64
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.getUrlEncoder().withoutPadding().encodeToString(rawMessageBytes);

            // Creo el mensaje de email
            Message message = new Message();

            // Añado el mensaje de email a la solicitud
            message.setRaw(encodedEmail);

            return message;
        }
        catch (Exception exception)
        {
            String errorMessage = "Error al configurar el envío del email";
            log.error(errorMessage, exception);
            throw new NotificationsServerException(Constants.ERR_GENERIC_EXCEPTION_CODE, errorMessage, exception);
        }
    }
}
