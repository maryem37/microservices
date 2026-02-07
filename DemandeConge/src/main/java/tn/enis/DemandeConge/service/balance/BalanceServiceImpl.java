package tn.enis.DemandeConge.service.balance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enis.DemandeConge.entity.Balance;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.repository.BalanceRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class BalanceServiceImpl implements BalanceService {

    private final BalanceRepository balanceRepository;

    @Override
    public Balance initializeBalance(Long userId) {
        // Règle : Un utilisateur ne peut avoir qu'une seule ligne de solde
        if (balanceRepository.existsByUserId(userId)) {
            throw new RuntimeException("Cet utilisateur a déjà un solde initialisé !");
        }

        Balance balance = new Balance();
        balance.setUserId(userId);
        balance.setAnnualBalance(21.0); // Valeur par défaut (ex: 21 jours)
        balance.setRecoveryBalance(0.0); // 0 par défaut

        return balanceRepository.save(balance);
    }

    @Override
    public Balance getBalanceByUserId(Long userId) {
        return balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Aucun solde trouvé pour l'utilisateur ID : " + userId));
    }

    @Override
    public Balance updateBalance(Long userId, double annualBalance, double recoveryBalance) {
        Balance existingBalance = getBalanceByUserId(userId);

        // Mise à jour des valeurs
        existingBalance.setAnnualBalance(annualBalance);
        existingBalance.setRecoveryBalance(recoveryBalance);

        return balanceRepository.save(existingBalance);
    }

    @Transactional
    public void debitBalance(Long userId, LeaveType type, Double days, Double hours) {

        Balance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Solde introuvable pour l'utilisateur " + userId));

        if (type == LeaveType.ANNUAL_LEAVE) {
            // Déduction Jours
            if (balance.getAnnualBalance() < days) {
                throw new RuntimeException("Solde annuel insuffisant lors de la validation finale.");
            }
            balance.setAnnualBalance(balance.getAnnualBalance() - days);

        } else if (type == LeaveType.RECOVERY_LEAVE) {
            // Déduction Heures
            if (balance.getRecoveryBalance() < hours) {
                throw new RuntimeException("Solde récupération insuffisant lors de la validation finale.");
            }
            balance.setRecoveryBalance(balance.getRecoveryBalance() - hours);
        }

        // Pour UNPAID ou AUTHORIZATION, on ne fait rien (ou on gère un compteur si besoin)

        balanceRepository.save(balance);
    }

    @Override
    public void deleteBalanceByUserId(Long userId) {
        Balance existingBalance = getBalanceByUserId(userId);
        balanceRepository.delete(existingBalance);
    }
}
