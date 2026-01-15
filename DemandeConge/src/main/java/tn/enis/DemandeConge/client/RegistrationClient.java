package tn.enis.DemandeConge.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.enis.DemandeConge.dto.UserDto;

@FeignClient(
        name = "auth-service",
        url = "http://localhost:9000"   // PORT DU MICRO-SERVICE AUTH
)
public interface RegistrationClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);
}
