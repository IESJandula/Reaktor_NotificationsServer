package es.iesjandula.reaktor.notifications_server.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestDto 
{
    /* Atributo - From */
	private String from;

    /* Atributo - To */
    private List<String> to;

    /* Atributo - CC */
    private List<String> cc;

    /* Atributo - BCC */
    private List<String> bcc;

    /* Atributo - Subject */
    private String subject;

    /* Atributo - Body */
    private String body;
	
}
