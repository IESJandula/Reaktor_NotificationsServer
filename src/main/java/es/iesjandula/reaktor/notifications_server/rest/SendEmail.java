package es.iesjandula.reaktor.notifications_server.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import es.iesjandula.reaktor.base.utils.BaseConstants;
import es.iesjandula.reaktor.notifications_server.dtos.EmailRequestDto;
import es.iesjandula.reaktor.notifications_server.services.EmailService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/email")
public class SendEmail {

    @Autowired
    private EmailService emailService;

    @RequestMapping(method = RequestMethod.POST, value = "/send")
    @PreAuthorize("hasRole('" + BaseConstants.ROLE_PROFESOR + "')")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDto emailRequestDto) {
        try {
            String response = this.emailService.sendEmail(
                emailRequestDto.getFrom(),
                emailRequestDto.getTo(),
                emailRequestDto.getCc(),
                emailRequestDto.getBcc(),
                emailRequestDto.getSubject(),
                emailRequestDto.getBody()
            );
            log.info("Correo enviado con éxito");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al enviar el email", e);
            return ResponseEntity.internalServerError()
                    .body("Error al enviar el email: " + e.getMessage());
        }
    }
}
