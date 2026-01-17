package tn.enis.DemandeConge.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.services.EmployeReq.EmployerReq;

@RestController
//@RequestMapping("/api/leave")
@RequestMapping("/api/employer/leave") // <- add /employer
@RequiredArgsConstructor
public class LeaveRequestController {
    private final EmployerReq employerReq;
    @PostMapping("/request")
    public ResponseEntity<String> createLeaveRequest(@RequestBody LeaveRequestDto leaveRequestDto) {
        boolean created = employerReq.leaveRequest(leaveRequestDto);
        if (created) {
            return ResponseEntity.ok("Leave request submitted successfully!");
        } else {
            return ResponseEntity.badRequest().body("Failed to submit leave request. User may not exist.");
        }
    }


}
