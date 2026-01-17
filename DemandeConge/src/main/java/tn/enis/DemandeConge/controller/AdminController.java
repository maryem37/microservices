package tn.enis.DemandeConge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.services.admin.AdminService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // Changer le statut d'une demande de congé
    @PutMapping("/leave/{id}/status/{status}")
    public ResponseEntity<?> changeLeaveStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        boolean success = adminService.changeLeaveStatus(id, status);

        if (success) {
            return ResponseEntity.ok("Leave request status updated successfully!");
        }
        return ResponseEntity.notFound().build();
    }

    // Récupérer les demandes en attente
    @GetMapping("/leave/pending")
    public ResponseEntity<?> getAllPendingLeaveRequests() {
        return ResponseEntity.ok(adminService.getAllPendingLeaveRequests());
    }

    // Récupérer toutes les demandes
    @GetMapping("/leave/all")
    public ResponseEntity<?> getAllLeaveRequests() {
        return ResponseEntity.ok(adminService.getAllLeaveRequests());
    }
}
