package tn.enis.conge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CongeeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CongeeApplication.class, args);
	}

}
