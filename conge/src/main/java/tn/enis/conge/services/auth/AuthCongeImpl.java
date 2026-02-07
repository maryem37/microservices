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
    @Transactional
    public void createDefaultAccounts() {

        // ADMINISTRATION
        if (userRepository.findByCin("00000000").isEmpty()) {
            User admin = new User();
            admin.setLastName("Administration");
            admin.setEmail("administration@test.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("admin"));
            admin.setUserRole(UserRole.Administration);
            admin.setCin("00000000");
            userRepository.save(admin);
            System.out.println("Admin account created");
        }
    }



    @Override
    public UserDto createEmployer(SignupRequest signupRequest) {

        // 2. VALIDATION : On vérifie tout de suite si l'ID est là
        if (signupRequest.getDepartmentId() == null) {
            throw new IllegalArgumentException("Erreur : Le département est obligatoire pour l'inscription.");
        }

        // 3. RÉCUPÉRATION : On va chercher le vrai département en base
        // Si l'ID n'existe pas, on arrête tout (Throw Exception)
        Department department = departmentRepository.findById(signupRequest.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Département introuvable avec l'ID : " + signupRequest.getDepartmentId()));

        User user = new User();
        user.setLastName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(signupRequest.getPassword()));
        user.setUserRole(signupRequest.getUserRole());
        user.setCin(signupRequest.getCin());

        // 4. ASSOCIATION : C'est l'étape CRUCIALE que tu avais oubliée !
        // Sans ça, la colonne department_id reste NULL en base
        user.setDepartment(department);

        // Sauvegarde (Le trigger @Formula du département se mettra à jour tout seul plus tard)
        User createdUser = userRepository.save(user);

        // Construction du DTO de réponse
        UserDto userDto = new UserDto();
        userDto.setId(createdUser.getId());
        userDto.setName(createdUser.getLastName());
        userDto.setEmail(createdUser.getEmail());
        userDto.setUserRole(createdUser.getUserRole());

        // 5. REMPLISSAGE SÉCURISÉ
        // On est sûr que getDepartment() n'est pas null car on l'a forcé à l'étape 3
        userDto.setDepartmentId(department.getId());
        userDto.setDepartmentName(department.getNameDepartment());

        return userDto;
    }
    @Override
    public boolean hasCustomerWithEmail(String email) {
        return userRepository.findFirstByEmail(email).isPresent();
    }
}
