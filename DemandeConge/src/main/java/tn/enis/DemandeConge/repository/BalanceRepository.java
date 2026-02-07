package tn.enis.DemandeConge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enis.DemandeConge.entity.Balance;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    // Essentiel pour récupérer le solde d'un employé spécifique
    Optional<Balance> findByUserId(Long userId);

    // Pour éviter de créer deux soldes pour le même employé
    boolean existsByUserId(Long userId);
}
