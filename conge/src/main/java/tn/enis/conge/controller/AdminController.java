package tn.enis.conge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.services.auth.AuthConge;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthConge authConge;

    // CRÉATION D'EMPLOYÉ (Réservé à l'Admin)
    @PostMapping("/create-employee")
    public ResponseEntity<?> createEmployee(@RequestBody SignupRequest signupRequest) {

        if (authConge.hasCustomerWithEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Error: User already exists with this email.");
        }

        UserDto createdUser = authConge.createEmployer(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}