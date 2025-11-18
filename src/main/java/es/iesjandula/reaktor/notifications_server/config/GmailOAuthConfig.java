package es.iesjandula.reaktor.notifications_server.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import es.iesjandula.reaktor.notifications_server.utils.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GmailOAuthConfig
{
	/** Factoría de JSON para la API de Google */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${reaktor.gmail.oauthCredentialsFile}")
    private String gmailOauthCredentialsFile;

	/**
	 * Bean de transporte HTTP para la API de Google
	 * @return NetHttpTransport
	 * @throws GeneralSecurityException - Excepción de seguridad general
	 * @throws IOException - Excepción de entrada/salida
	 */
    @Bean
    public NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException
    {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Bean de credenciales de OAuth para la API de Google
     * @return GoogleClientSecrets
     * @throws IOException - Excepción de entrada/salida
     */
    @Bean
    public GoogleClientSecrets googleClientSecrets() throws IOException
    {
        try (Reader clientSecretsReader = new FileReader(this.gmailOauthCredentialsFile))
        {
            return GoogleClientSecrets.load(JSON_FACTORY, clientSecretsReader);
        }
    }

    /**
     * Bean de flujo de autorización para la API de Google
     * @param httpTransport - Transporte HTTP para la API de Google
     * @param clientSecrets - Credenciales de OAuth para la API de Google
     * @return GoogleAuthorizationCodeFlow
     * @throws IOException - Excepción de entrada/salida
     */
    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(NetHttpTransport httpTransport, GoogleClientSecrets clientSecrets) throws IOException
    {
        File tokenDir = new File(Constants.GOOGLE_TOKEN_DIRECTORY_PATH);

        if (!tokenDir.exists())
        {
            boolean created = tokenDir.mkdirs();
            if (!created)
            {
                String errorString = "No se ha podido crear el directorio de tokens en " + tokenDir.getAbsolutePath();

                log.error(errorString);
                throw new IOException(errorString);
            }
        }

        return new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Constants.GOOGLE_SCOPES)
                                              .setDataStoreFactory(new FileDataStoreFactory(tokenDir))
                                              .setAccessType("offline")
                                              .setApprovalPrompt("force")
                                              .build();
    }

    /**
     * Bean principal de credenciales para usar en el servicio de envío de correo.
     * 
     * - Si ya hay token en tokens/, lo carga y refresca si hace falta.
     * - Si NO hay token, lanza excepción indicando que primero hay que ir a /gmail/authorize.
     */
    @Bean
    public Credential gmailCredential(GoogleAuthorizationCodeFlow flow) throws IOException
    {
        // Intentamos cargar credenciales existentes
        Credential credential = flow.loadCredential(Constants.GOOGLE_USER_ID);

        if (credential == null)
        {
            String errorString = "No hay token de autorización disponible. " +
                                 "Primero debes completar el flujo OAuth en el navegador: " +
                                 "visita la URL /gmail/authorize de este servidor para autorizar la cuenta de Gmail. " +
                                 "Una vez completado, reinicia (o vuelve a llamar) para que se carguen las credenciales.";

            log.error(errorString);
            throw new IOException(errorString);
        }

        try
        {
			// Si hay token y está cerca de expirar, intentamos refrescarlo
            if (credential.getExpiresInSeconds() != null && credential.getExpiresInSeconds() < 60 && credential.getRefreshToken() != null)
            {
                boolean refreshed = credential.refreshToken();
                if (!refreshed)
                {
                    String errorString = "No se ha podido refrescar el token de Gmail. Quizás haya que volver a autorizar la aplicación.";

                    log.error(errorString);
                    throw new IOException(errorString);
                }
            }
        }
        catch (IOException ioException)
        {
            String errorString = "Error al refrescar el token de Gmail: " + ioException.getMessage();

            log.error(errorString, ioException);
            throw ioException;
        }

        return credential ;
    }
}
