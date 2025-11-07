package es.iesjandula.reaktor.notifications_server.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.iesjandula.reaktor.notifications_server.models.Birthday;
import es.iesjandula.reaktor.notifications_server.repository.IBirthdayRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BirthdayScheduler 
{
	
	@Autowired
	private IBirthdayRepository birthdayRepository ;

	@Scheduled(cron = "0 0 0 * * *", zone = "Europe/Madrid")
	public void greetings()
	{
		
		LocalDate fechaActual = LocalDate.now();
		List<Birthday> birthdays = this.birthdayRepository.findAll() ;
		
		for (Birthday b : birthdays) 
		{
			if (b.getFechaNacimiento().getMonth() == fechaActual.getMonth() && b.getFechaNacimiento().getDayOfMonth() == fechaActual.getDayOfMonth())
			{
				b.setGreetings(true) ;
			} else 
			{
				b.setGreetings(false) ;
			}
		}
		
		this.birthdayRepository.saveAll(birthdays) ;
		log.info("Greetings actualizados correctamente");
		
	}
	
}
