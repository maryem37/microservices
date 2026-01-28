package tn.enis.DemandeConge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.services.admin.AdminService;
import tn.enis.conge.enums.UserRole;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // =============================================
    // Approve a leave request (TeamLeader or Admin)
    // =============================================
    @PutMapping("/leave/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String note, // optional note from manager
            @RequestParam UserRole role                  // current user role
    ) {
        try {
            boolean success = adminService.approveLeave(id, note, role);
            if (success) return ResponseEntity.ok("Leave request approved successfully!");
            return ResponseEntity.badRequest().body("Unable to approve the leave request");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    // =============================================
    // Get all pending leave requests
    // =============================================
    @GetMapping("/leave/pending")
    public ResponseEntity<?> getAllPendingLeaveRequests() {
        return ResponseEntity.ok(adminService.getAllPendingLeaveRequests());
    }

    // =============================================
    // Get all leave requests
    // =============================================
    @GetMapping("/leave/all")
    public ResponseEntity<?> getAllLeaveRequests() {
        return ResponseEntity.ok(adminService.getAllLeaveRequests());
    }
}
