package tn.enis.DemandeConge.services.admin;
import tn.enis.conge.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.repository.LeaveRequestRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final LeaveRequestRepository leaveRequestRepository;
    private boolean canApprove(UserRole role, LeaveRequest leaveRequest) {
        if (role == UserRole.TeamLeader) {
            // TeamLeader peut approuver seulement s'il n'a pas encore approuvé
            return leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false) == false;
        }
        if (role == UserRole.Administration) {
            // Administration peut approuver seulement si TeamLeader a déjà approuvé
            return leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false) == true
                    && leaveRequest.getApprovalChain().getOrDefault(UserRole.Administration, false) == false;
        }
        return false;
    }


    @Override
    public boolean approveLeave(Long leaveRequestId, String note, UserRole currentUserRole) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // Check the role chain
        if (!canApprove(currentUserRole, leaveRequest)) {
            throw new RuntimeException("Exception 17: Waiting for validation");
        }

        // Check the current state (Pending or InProgress only)
        if (!(leaveRequest.getState() == RequestState.Pending || leaveRequest.getState() == RequestState.InProgress)) {
            throw new RuntimeException("Exception 18: Validation failed");
        }

        // Check if already approved by this role
        if (leaveRequest.getApprovalChain().getOrDefault(currentUserRole, false)) {
            throw new RuntimeException("Exception 19: Leave request already approved by this user");
        }

        // Approve the request for this role
        leaveRequest.getApprovalChain().put(currentUserRole, true);
        if (note != null && !note.isEmpty()) leaveRequest.setManagerNote(note);

        // Determine new state
        boolean allApproved = leaveRequest.getApprovalChain().values().stream().allMatch(Boolean::booleanValue);
        if (allApproved) {
            leaveRequest.setState(RequestState.Approved);  // Only final approver sets Approved
        } else {
            leaveRequest.setState(RequestState.InProgress); // Otherwise InProgress
        }

        leaveRequestRepository.save(leaveRequest);
        return true;
    }

    @Override
    public String rejectLeave(
            Long leaveRequestId,
            UserRole role,
            String reason,
            String observation
    ) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // ❌ Exception 23: reason is mandatory
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Exception 23: A rejection reason must be provided!");
        }

        // ❌ Exception 24: already rejected
        if (leaveRequest.getState() == RequestState.Rejected) {
            throw new RuntimeException("Exception 24: Leave request has already been rejected");
        }

        // ❌ Exception 25: already approved or cancelled
        if (leaveRequest.getState() == RequestState.Approved ||
                leaveRequest.getState() == RequestState.Cancelled) {
            throw new RuntimeException("Exception 25: Leave request is already approved or cancelled");
        }

        // ❌ Exception 17: respect approval chain
        if (role == UserRole.Administration &&
                !leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false)) {
            throw new RuntimeException("Exception 17: Waiting for previous approval in the chain");
        }

        // ❌ Exception 19: already processed by this user
        if (leaveRequest.getApprovalChain().getOrDefault(role, false)) {
            throw new RuntimeException("Exception 19: This request has already been processed by this user");
        }

        // ✅ FINAL REJECTION
        leaveRequest.setState(RequestState.Rejected);
        leaveRequest.setRejectedBy(role);
        leaveRequest.setRejectionDate(LocalDate.now());
        leaveRequest.setRejectionReason(reason);
        leaveRequest.setRejectionObservation(observation);

        leaveRequestRepository.save(leaveRequest);

        return "Leave request rejected successfully";
    }

    @Override
    public List<LeaveRequestDto> getAllPendingLeaveRequests() {
        return leaveRequestRepository.findAllByState(RequestState.Pending)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private LeaveRequestDto convertToDto(LeaveRequest leaveRequest) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leaveRequest.getId());
        dto.setFromDate(leaveRequest.getFromDate());
        dto.setToDate(leaveRequest.getToDate());
        dto.setFromTime(leaveRequest.getFromTime());
        dto.setToTime(leaveRequest.getToTime());
        dto.setDays(leaveRequest.getDays());
        dto.setState(leaveRequest.getState());
        dto.setNote(leaveRequest.getNote());
        dto.setType(leaveRequest.getType());
        dto.setUserId(leaveRequest.getUserId());
        return dto;
    }
}
