package tn.enis.conge.controller;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.conge.dto.UserDto;
import tn.enis.conge.entity.User;
import tn.enis.conge.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();

        // Map to DTO
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setUserRole(user.getUserRole());
        // --- Remplissage manuel des infos département ---
        if (user.getDepartment() != null) {
            userDto.setDepartmentId(user.getDepartment().getId()); // On stocke l'ID
            userDto.setDepartmentName(user.getDepartment().getNameDepartment()); // On stocke le nom
        }
        // Don't expose password!

        return ResponseEntity.ok(userDto);
    }
    @GetMapping("/search-ids")
    public List<Long> searchUserIds(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) String deptName) {

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (firstName != null && !firstName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("prenom")), "%" + firstName.toLowerCase() + "%"));
            }
            if (lastName != null && !lastName.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + lastName.toLowerCase() + "%"));
            }
            if (cin != null && !cin.isEmpty()) {
                predicates.add(cb.equal(root.get("cin"), cin));
            }
            if (deptName != null && !deptName.isEmpty()) {
                // Jointure avec la table Department (attribut nameDepartment)
                predicates.add(cb.equal(root.join("department").get("nameDepartment"), deptName));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // On ne récupère que les IDs pour limiter le volume de données transférées
        return userRepository.findAll(spec).stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }
}