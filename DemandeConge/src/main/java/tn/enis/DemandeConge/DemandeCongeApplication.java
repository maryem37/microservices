package tn.enis.DemandeConge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "tn.enis.DemandeConge.client")
public class DemandeCongeApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemandeCongeApplication.class, args);
	}
}
