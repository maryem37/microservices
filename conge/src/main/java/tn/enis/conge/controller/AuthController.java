package tn.enis.conge.controller;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import tn.enis.conge.dto.*;
import tn.enis.conge.entity.ChangePasswordAfterReset;
import tn.enis.conge.entity.User;
import tn.enis.conge.repository.UserRepository;
import tn.enis.conge.services.auth.AuthCongeImpl;
import tn.enis.conge.services.jwt.UserServiceImpl;
import tn.enis.conge.utils.JWTutil;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserServiceImpl userService;
    private final JWTutil jwtUtil;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthCongeImpl authCongeService;

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

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {
        System.out.println("Reset request for: " + email);
        try {
            authCongeService.resetPassword(email);
            System.out.println("Reset successful for: " + email);
            return ResponseEntity.ok("An email has been sent to " + email + " with a new password.");
        } catch (MessagingException | IOException e) {
            System.out.println("Error sending email to " + email + ": " + e.getMessage());
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General error for " + email + ": " + e.getMessage());
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePasswordAfterReset(@RequestBody ChangePasswordAfterReset request) {
        System.out.println("Request to change password after reset");
        System.out.println("Email: " + request.getEmail());
        System.out.println("TempPassword provided: " + (request.getTemporaryPassword() != null ? "Yes" : "No"));
        System.out.println("NewPassword provided: " + (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty() ? "Yes" : "No"));

        try {
            boolean success = authCongeService.changePasswordAfterReset(request);
            if (success) {
                System.out.println("Password change successful for: " + request.getEmail());
                return ResponseEntity.ok("Password updated successfully!");
            } else {
                System.out.println("Incorrect temporary password for: " + request.getEmail());
                return ResponseEntity.status(400).body("Incorrect temporary password.");
            }
        } catch (Exception e) {
            System.out.println("Error changing password for " + request.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // 1. On vide le contexte de sécurité du serveur
        SecurityContextHolder.clearContext();

        // 2. On informe le serveur (logs)
        System.out.println("Logout success: Security context cleared.");

        // 3. On renvoie une réponse propre au Frontend
        return ResponseEntity.ok("Successfully logged out. Remember to delete the JWT on the client side.");
    }

}