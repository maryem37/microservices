package tn.enis.DemandeConge.services.admin;

import tn.enis.DemandeConge.dto.LeaveRequestDto;

import java.util.List;

public interface AdminService {

    boolean changeLeaveStatus(Long leaveRequestId, String status);

    List<LeaveRequestDto> getAllPendingLeaveRequests();

    List<LeaveRequestDto> getAllLeaveRequests();
}
