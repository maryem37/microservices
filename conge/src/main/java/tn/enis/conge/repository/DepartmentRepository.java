package tn.enis.conge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enis.conge.entity.Department;

import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // 1. Méthode standard fournie par défaut (tu n'as pas besoin de l'écrire, mais elle existe) :
    // Optional<Department> findById(Long id);

    // 2. Méthode utile si tu veux un jour chercher par nom (ex: "Informatique")
    // Spring générera la requête SQL automatiquement grâce au nom de la méthode.
    Optional<Department> findByNameDepartment(String nameDepartment);

    // 3. Méthode utile pour vérifier si un département existe avant de le créer
    boolean existsByNameDepartment(String nameDepartment);
}
