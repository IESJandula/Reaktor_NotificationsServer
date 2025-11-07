package es.iesjandula.reaktor.notifications_server.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

@Configuration
public class EmailConfig 
{
	
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/gmail.send");
	private static final String TOKEN_DIRECTORY_PATH = "tokens";
	
	@Value("${reaktor.googleCredentialsFile}")
	private String googleCredentialsFile ;

	@Value("${reaktor.gmail.oauthCredentialsFile}")
	private String gmailOauthCredentialsFile;

	@Bean
	public Credential gmailCredentials() throws IOException, GeneralSecurityException
	{
		// Cargar las credenciales OAuth 2.0 desde el archivo JSON
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		
		Reader clientSecretsReader = new FileReader(this.gmailOauthCredentialsFile);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretsReader);

		// Crear el flujo de autorización
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
			httpTransport, 
			JSON_FACTORY, 
			clientSecrets, 
			SCOPES)
			.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKEN_DIRECTORY_PATH)))
			.setAccessType("offline")
			.setApprovalPrompt("force")
			.build();

		// Obtener credenciales (si ya existe un token almacenado, lo usará; si no, abrirá el navegador)
		// Los tokens se guardan automáticamente en la carpeta "tokens" por FileDataStoreFactory
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

		return credential;
	}
	
}
