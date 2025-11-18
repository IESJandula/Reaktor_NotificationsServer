package es.iesjandula.reaktor.notifications_server.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.utils.Constants;
import es.iesjandula.reaktor.notifications_server.utils.NotificationsServerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/notifications/gmail")
public class GmailOAuthController
{
    @Autowired
    private GoogleAuthorizationCodeFlow flow;

    /** URL de redirección de OAuth2 */
    @Value("${reaktor.gmail.oauthRedirectUri}")
    private String oauthRedirectUri;

    // Página a la que redirigimos después de éxito (puede ser tu frontend)
    @Value("${reaktor.gmail.postAuthRedirect:/}")
    private String postAuthRedirect;

    /**
     * Paso 1: redirige a Google para pedir consentimiento.
     * 
     * Ejemplo: GET https://tu-dominio/gmail/authorize
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
    @RequestMapping(value = "/authorize", method = RequestMethod.GET)
    public RedirectView authorize()
    {
        String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(this.oauthRedirectUri).build();

        log.info("Iniciando autorización de Gmail. Redirigiendo a: {}", authorizationUrl);

        return new RedirectView(authorizationUrl);
    }

    /**
     * Paso 2: callback de Google con el parámetro 'code'.
     * 
     * Google redirige aquí tras aceptar en la pantalla de consentimiento.
     * @throws IOException - Excepción de entrada/salida
     * @throws NotificationsServerException - Excepción de servidor de notificaciones
     */
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_ADMINISTRADOR + "')")
    @RequestMapping(value = "/oauth2callback", method = RequestMethod.GET)
    public RedirectView oauth2callback(@RequestParam(value = "code", required = false) String code, 
                                       @RequestParam(value = "error", required = false) String error) throws IOException
    {
        if (error != null)
        {
            log.error("Error devuelto por Google en OAuth2: {}", error);
            
            return new RedirectView(this.postAuthRedirect + "?gmail_oauth_error=" + error);
        }

        if (code == null)
        {
            log.error("No se recibió parámetro 'code' en el callback de Gmail OAuth2");
            return new RedirectView(this.postAuthRedirect + "?gmail_oauth_error=missing_code");
        }

        log.info("Recibido código de autorización de Gmail. Intercambiando por tokens...");

        // Intercambiamos el código por tokens y los guardamos en la DataStore (tokens/)
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(code).setRedirectUri(this.oauthRedirectUri).execute();

        // Guardamos las credenciales en la DataStore (tokens/)
        Credential credential = flow.createAndStoreCredential(tokenResponse, Constants.GOOGLE_USER_ID);

        if (credential != null && credential.getAccessToken() != null)
        {
            log.info("Credenciales de Gmail almacenadas correctamente en la carpeta 'tokens/'.");
        }
        else
        {
            log.warn("No se han podido almacenar correctamente las credenciales de Gmail.");
        }

        // Redirigimos a donde tú quieras (por defecto "/")
        return new RedirectView(postAuthRedirect + "?gmail_oauth_success=true");
    }
}
