package es.iesjandula.reaktor.notifications_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Francisco Manuel Benítez Chico
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"es.iesjandula"})
public class ReaktorNotificationsServerApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(ReaktorNotificationsServerApplication.class, args);
	}
}
