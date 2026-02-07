package tn.enis.DemandeConge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.dto.LeaveRequestDetailsDto;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.dto.LeaveRequestSearchCriteria;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.service.LeaveRequest.LeaveRequestService;
import tn.enis.conge.enums.UserRole;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;


    @PostMapping("/create")
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequestDto dto) {
        log.info("Réception d'une demande de congé pour l'utilisateur ID : {}", dto.getUserId());

        // Appel au service (qui contient toute la logique de validation et calcul)
        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(dto);

        // Retourne l'objet créé avec le code HTTP 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }

    // =============================================
    // Approve a leave request (TeamLeader or Admin)
    // =============================================
    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String note, // optional note from manager
            @RequestParam UserRole role                  // current user role
    ) {
        try {
            boolean success = leaveRequestService.approveLeave(id, note, role);
            if (success) return ResponseEntity.ok("Leave request approved successfully!");
            return ResponseEntity.badRequest().body("Unable to approve the leave request");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id,
            @RequestParam UserRole role,
            @RequestParam String reason,
            @RequestParam(required = false) String observation
    ) {
        try {
            return ResponseEntity.ok(
                    leaveRequestService.rejectLeave(id, role, reason, observation)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelLeaveRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String observation) {
        String result = leaveRequestService.cancelLeaveRequest(id, observation);
        if (result.equals("Leave request cancelled successfully.")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }


    @GetMapping("/search")
    public List<LeaveRequestDetailsDto> searchLeaveRequests(
            @RequestParam Long currentUserId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String cin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String deptName,
            @RequestParam(required = false) LeaveType type,
            @RequestParam(required = false) List<RequestState> states,
            @RequestParam(required = false) UserRole validationLevel) {

        // Construction du critère avec le pattern Builder
        LeaveRequestSearchCriteria criteria = LeaveRequestSearchCriteria.builder()
                .currentUserId(currentUserId)
                .firstName(firstName)
                .lastName(lastName)
                .cin(cin)
                .fromDate(fromDate)
                .toDate(toDate)
                .deptName(deptName)
                .type(type)
                .states(states)
                .validationLevel(validationLevel)
                .build();

        return leaveRequestService.searchLeaveRequests(criteria);
    }


}
