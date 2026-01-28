package tn.enis.DemandeConge.services.admin;
import tn.enis.conge.enums.UserRole;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.repository.LeaveRequestRepository;

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
