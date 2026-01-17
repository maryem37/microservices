package tn.enis.conge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import tn.enis.conge.dto.AuthenticationRequest;
import tn.enis.conge.dto.AuthenticationResponse;
import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.entity.User;
import tn.enis.conge.repository.UserRepository;
import tn.enis.conge.services.auth.AuthConge;
import tn.enis.conge.services.jwt.UserServiceImpl;
import tn.enis.conge.utils.JWTutil;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthConge authConge;
    private final UserServiceImpl userService;
    private final JWTutil jwtUtil;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    // ---------- SIGNUP ----------
    @PostMapping("/signup")
    public ResponseEntity<?> signupEmploye(@RequestBody SignupRequest signupRequest) {

        if (authConge.hasCustomerWithEmail(signupRequest.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Customer already exists with this email");
        }

        UserDto createdUser = authConge.createEmployer(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // ---------- LOGIN ----------
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest authenticationRequest) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Incorrect email or password");
        }

        UserDetails userDetails =
                userService.loadUserByUsername(authenticationRequest.getEmail());

        Optional<User> optionalUser =
                userRepository.findFirstByEmail(userDetails.getUsername());

        String jwt = jwtUtil.generateToken(userDetails);

        AuthenticationResponse response = new AuthenticationResponse();
        if (optionalUser.isPresent()) {
            response.setJwt(jwt);
            response.setUserId(optionalUser.get().getId());
            response.setUserRole(optionalUser.get().getUserRole());
        }

        return ResponseEntity.ok(response);
    }




}
