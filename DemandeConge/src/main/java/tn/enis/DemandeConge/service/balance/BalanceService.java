package tn.enis.DemandeConge.service.balance;

import tn.enis.DemandeConge.entity.Balance;
import tn.enis.DemandeConge.enums.LeaveType;

public interface BalanceService {

    // Créer un solde initial (souvent lors de la création de l'employé)
    Balance initializeBalance(Long userId);

    // Récupérer le solde d'un utilisateur
    Balance getBalanceByUserId(Long userId);

    // Mettre à jour les soldes (Annuel ou Récupération)
    Balance updateBalance(Long userId, double annualBalance, double recoveryBalance);

    void debitBalance(Long userId, LeaveType type, Double days, Double hours);

    // Supprimer un solde (si l'utilisateur est supprimé par ex)
    void deleteBalanceByUserId(Long userId);
}
