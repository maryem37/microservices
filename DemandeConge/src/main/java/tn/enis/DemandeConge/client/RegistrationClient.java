package tn.enis.DemandeConge.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.dto.UserDto;

import java.util.List;

@FeignClient(
        name = "auth-service",
        url = "http://localhost:9000"   // PORT DU MICRO-SERVICE AUTH
)
public interface RegistrationClient {

    @GetMapping("/api/users/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/users/search-ids")
    List<Long> searchUserIds(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "cin", required = false) String cin,
            @RequestParam(value = "deptName", required = false) String deptName
    );

}
