package tn.enis.DemandeConge.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.enis.DemandeConge.dto.UserDto;

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
