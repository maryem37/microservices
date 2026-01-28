package tn.enis.conge.services.auth;


import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.entity.User;
import tn.enis.conge.enums.UserRole;
import tn.enis.conge.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthCongeImpl implements AuthConge{

    private final UserRepository userRepository;


    @PostConstruct
    @Transactional
    public void createDefaultAccounts() {

        // ADMINISTRATION
        if (userRepository.findByCin("00000000").isEmpty()) {
            User admin = new User();
            admin.setName("Administration");
            admin.setEmail("administration@test.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("admin"));
            admin.setUserRole(UserRole.Administration);
            admin.setCin("00000000");
            userRepository.save(admin);
            System.out.println("Admin account created");
        }

        // TEAM LEADER
        if (userRepository.findByCin("11111111").isEmpty()) {
            User teamLeader = new User();
            teamLeader.setName("TeamLeader");
            teamLeader.setEmail("teamleader@test.com");
            teamLeader.setPassword(new BCryptPasswordEncoder().encode("teamleader123"));
            teamLeader.setUserRole(UserRole.TeamLeader);
            teamLeader.setCin("11111111");
            userRepository.save(teamLeader);
            System.out.println("TeamLeader account created");
        }
    }



    @Override
    public UserDto createEmployer(SignupRequest signupRequest) {
        User user=new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(signupRequest.getPassword()));

        user.setUserRole(UserRole.Employer);
       user.setCin(signupRequest.getCin());


        User createdUser=userRepository.save(user);
        UserDto userDto = new UserDto();
       //     userDto.setId(createdUser.getId());
        userDto.setId(createdUser.getId());
        userDto.setName(createdUser.getName());
        userDto.setEmail(createdUser.getEmail());
        userDto.setUserRole(createdUser.getUserRole());


// ⚠️ NE PAS exposer le mot de passe
        return userDto;

    }

    @Override
    public boolean hasCustomerWithEmail(String email) {
        return userRepository.findFirstByEmail(email).isPresent();
    }
}
