package es.iesjandula.reaktor.notifications_server.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
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
@Profile("LOCAL")
public class GmailOAuthConfigLocal
{
    /** Factoría de JSON para la API de Google */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /** Fichero de credenciales OAuth para la API de Google */
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
     * Necesario para que GmailOAuthController pueda inyectarlo
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
                                              .setAccessType(Constants.GOOGLE_ACCESS_TYPE_OFFLINE)
                                              .setApprovalPrompt(Constants.GOOGLE_APPROVAL_PROMPT_FORCE)
                                              .build();
    }

    /**
     * Bean principal de credenciales para usar en el servicio de envío de correo.
     * 
     * En LOCAL: Obtiene credenciales (si ya existe un token almacenado, lo usará; si no, abrirá el navegador).
     * Los tokens se guardan automáticamente en la carpeta "tokens" por FileDataStoreFactory.
     * 
     * @param flow - Flujo de autorización para la API de Google
     * @return Credential
     * @throws IOException - Excepción de entrada/salida
     * @throws GeneralSecurityException - Excepción de seguridad general
     */
    @Bean
    public Credential gmailCredentials(GoogleAuthorizationCodeFlow flow) throws IOException, GeneralSecurityException
    {
        // Obtener credenciales (si ya existe un token almacenado, lo usará; si no, abrirá el navegador)
        // Los tokens se guardan automáticamente en la carpeta "tokens" por FileDataStoreFactory
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize(Constants.GOOGLE_USER_ID);

        return credential;
    }
}
