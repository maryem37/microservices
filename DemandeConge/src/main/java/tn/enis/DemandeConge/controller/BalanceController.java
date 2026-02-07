package tn.enis.DemandeConge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.entity.Balance;
import tn.enis.DemandeConge.service.balance.BalanceService;


@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    // 1. Initialiser un solde pour un utilisateur (POST /api/balances/init/{userId})
    @PostMapping("/init/{userId}")
    public ResponseEntity<Balance> initializeBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(balanceService.initializeBalance(userId));
    }

    // 2. Consulter le solde d'un utilisateur (GET /api/balances/{userId})
    @GetMapping("/{userId}")
    public ResponseEntity<Balance> getBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(balanceService.getBalanceByUserId(userId));
    }

    // 3. Modifier le solde manuellement (Admin / RH) (PUT /api/balances/{userId})
    // On passe les nouvelles valeurs en RequestParam ou dans un Body (ici RequestParam pour faire simple)
    @PutMapping("/{userId}")
    public ResponseEntity<Balance> updateBalance(
            @PathVariable Long userId,
            @RequestParam double annual,
            @RequestParam double recovery) {

        return ResponseEntity.ok(balanceService.updateBalance(userId, annual, recovery));
    }

    // 4. Supprimer un solde
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteBalance(@PathVariable Long userId) {
        balanceService.deleteBalanceByUserId(userId);
        return ResponseEntity.ok("Solde supprimé avec succès.");
    }
}
