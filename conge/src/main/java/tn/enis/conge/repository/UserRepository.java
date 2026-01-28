package tn.enis.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enis.conge.entity.User;
import tn.enis.conge.enums.UserRole;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findFirstByEmail(String email);
    Optional<User> findByCin(String cin); // <- à ajouter !

    User findByUserRole(UserRole userRole);
}
