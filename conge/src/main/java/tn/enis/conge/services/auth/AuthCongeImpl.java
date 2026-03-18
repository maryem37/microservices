package tn.enis.conge.services.auth;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tn.enis.conge.configuration.JwtAuthenthificationFilter;
import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.email.EmailService;
import tn.enis.conge.entity.ChangePasswordAfterReset;
import tn.enis.conge.entity.Department;
import tn.enis.conge.entity.User;
import tn.enis.conge.enums.UserRole;
import tn.enis.conge.repository.DepartmentRepository;
import tn.enis.conge.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCongeImpl implements AuthConge {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtAuthenthificationFilter jwtAuthenthificationFilter;

    @PostConstruct
    public void createDefaultAdmin() {
        if (userRepository.findFirstByEmail("admin@test.com").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
            admin.setUserRole(UserRole.Administration);
            admin.setCin("00000000");
            admin.setIsTemporaryPassword(false);
            userRepository.save(admin);
            System.out.println("Default Admin account created.");
        }
    }

    @Override
    public UserDto createEmployer(SignupRequest signupRequest) {
        // 1. Department Validation
        if (signupRequest.getDepartmentId() == null) {
            throw new IllegalArgumentException("Error: Department ID is required.");
        }

        Department department = departmentRepository.findById(signupRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Error: Department not found with ID: " + signupRequest.getDepartmentId()));

        // 2. User Entity Creation
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setCin(signupRequest.getCin());
        user.setNumTel(signupRequest.getNumTel());
        user.setUserRole(signupRequest.getUserRole());
        user.setDepartment(department);
        user.setIsTemporaryPassword(false);

        String rawPassword = (signupRequest.getPassword() != null && !signupRequest.getPassword().isEmpty())
                ? signupRequest.getPassword()
                : "12345678";
        user.setPassword(passwordEncoder.encode(rawPassword));

        // 3. Save
        User createdUser = userRepository.save(user);

        // 4. Mapping to DTO
        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());
        userDto.setFirstName(createdUser.getFirstName());
        userDto.setLastName(createdUser.getLastName());
        userDto.setEmail(createdUser.getEmail());
        userDto.setNumTel(createdUser.getNumTel());
        userDto.setUserRole(createdUser.getUserRole());
        userDto.setDepartmentId(department.getId());
        userDto.setDepartmentName(department.getNameDepartment());

        return userDto;
    }

    @Override
    public boolean hasCustomerWithEmail(String email) {
        return userRepository.findFirstByEmail(email).isPresent();
    }

    /**
     * Generates a secure random temporary password
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Password Reset - Sends a temporary password via email
     */
    public void resetPassword(String email) throws MessagingException, IOException {
        System.out.println("Starting resetPassword for email: " + email);

        Optional<User> userOpt = userRepository.findFirstByEmail(email);
        if (userOpt.isEmpty()) {
            System.out.println("Error: User not found for email: " + email);
            throw new BadCredentialsException("User with this email not found.");
        }

        User user = userOpt.get();
        System.out.println("User found: " + user.getEmail());

        String tempPassword = generateRandomPassword();
        System.out.println("Temporary password generated: " + tempPassword);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), tempPassword);
            System.out.println("Email sent successfully");
        } catch (Exception e) {
            System.out.println("Error while sending email: " + e.getMessage());
            throw e;
        }

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setIsTemporaryPassword(true); // Flag active
        userRepository.save(user);
        System.out.println("Temporary password saved in database");
    }

    /**
     * Changes password after reset process
     */
    public boolean changePasswordAfterReset(ChangePasswordAfterReset request) {
        System.out.println("Starting changePasswordAfterReset");
        System.out.println("Email: " + request.getEmail());

        Optional<User> userOpt = userRepository.findFirstByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            System.out.println("Error: User not found for email: " + request.getEmail());
            throw new BadCredentialsException("User not found.");
        }

        User user = userOpt.get();
        System.out.println("User found: " + user.getEmail());

        boolean correctTempPassword = passwordEncoder.matches(request.getTemporaryPassword(), user.getPassword());
        System.out.println("Temporary password verification: " + (correctTempPassword ? "Correct" : "Incorrect"));

        if (!correctTempPassword) {
            return false;
        }

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            System.out.println("Update mode - Setting new password");
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setIsTemporaryPassword(false); // Flag deactivated
            userRepository.save(user);
            System.out.println("New password defined and saved");
        } else {
            System.out.println("Skip mode - Keeping temporary password");
            // isTemporaryPassword remains true
        }

        System.out.println("changePasswordAfterReset completed successfully");
        return true;
    }
}