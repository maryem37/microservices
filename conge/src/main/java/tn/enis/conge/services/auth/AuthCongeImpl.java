package tn.enis.conge.services.auth;


import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.entity.Department;
import tn.enis.conge.entity.User;
import tn.enis.conge.enums.UserRole;
import tn.enis.conge.repository.DepartmentRepository;
import tn.enis.conge.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCongeImpl implements AuthConge{

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

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
            userRepository.save(admin);
            System.out.println("Default Admin account created.");
        }
    }


    @Override
    public UserDto createEmployer(SignupRequest signupRequest) {

        // 1. Validation Département
        if (signupRequest.getDepartmentId() == null) {
            throw new IllegalArgumentException("Error: Department ID is required.");
        }

        Department department = departmentRepository.findById(signupRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Error: Department not found with ID: " + signupRequest.getDepartmentId()));

        // 2. Création de l'entité User (Mapping complet)
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setCin(signupRequest.getCin());
        user.setNumTel(signupRequest.getNumTel());
        user.setUserRole(signupRequest.getUserRole());

        // Association du département
        user.setDepartment(department);

        // Mot de passe (Saisi par l'admin ou par défaut "12345678" si vide)
        String rawPassword = (signupRequest.getPassword() != null && !signupRequest.getPassword().isEmpty())
                ? signupRequest.getPassword()
                : "12345678";
        user.setPassword(new BCryptPasswordEncoder().encode(rawPassword));

        // 3. Sauvegarde
        User createdUser = userRepository.save(user);

        // 4. Mapping vers UserDto pour le retour
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
}
