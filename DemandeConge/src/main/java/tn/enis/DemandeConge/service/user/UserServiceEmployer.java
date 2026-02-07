package tn.enis.DemandeConge.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import tn.enis.DemandeConge.client.RegistrationClient;
import tn.enis.DemandeConge.dto.UserDto;
import tn.enis.conge.entity.User;
import tn.enis.conge.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceEmployer {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get user info from registration microservice by userId
     */
    public UserDto getUserById(Long userId) {
        String url = "http://localhost:9000/api/users/" + userId; // registration service URL
        return restTemplate.getForObject(url, UserDto.class);
    }



}
