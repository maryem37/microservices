package tn.enis.DemandeConge.services.admin;

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

    @Override
    public boolean changeLeaveStatus(Long leaveRequestId, String status) {

        Optional<LeaveRequest> optionalLeaveRequest =
                leaveRequestRepository.findById(leaveRequestId);

        if (optionalLeaveRequest.isEmpty()) {
            System.err.println("❌ Leave request not found with ID: " + leaveRequestId);
            return false;
        }

        LeaveRequest leaveRequest = optionalLeaveRequest.get();

        RequestState newState;

        switch (status.toUpperCase()) {
            case "PENDING":
                newState = RequestState.Pending;
                break;

            case "APPROVED":
                newState = RequestState.Approved;
                break;

            case "REJECTED":
                newState = RequestState.Rejected;
                break;

            case "INPROGRESS":
            case "IN_PROGRESS":
                newState = RequestState.InProgress;
                break;

            default:
                System.err.println("❌ Invalid status: " + status);
                System.err.println("✔ Allowed values: Pending, Approved, Rejected, InProgress");
                return false;
        }

        leaveRequest.setState(newState);
        leaveRequestRepository.save(leaveRequest);

        System.out.println("✅ Status updated to " + newState +
                " for leave request ID " + leaveRequestId);

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
