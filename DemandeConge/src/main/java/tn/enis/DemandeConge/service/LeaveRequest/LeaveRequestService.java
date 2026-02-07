package tn.enis.DemandeConge.service.LeaveRequest;

import tn.enis.DemandeConge.dto.LeaveRequestDetailsDto;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.dto.LeaveRequestSearchCriteria;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.conge.enums.UserRole;


import java.util.List;

public interface LeaveRequestService {

    LeaveRequest createLeaveRequest(LeaveRequestDto dto);

    String cancelLeaveRequest(Long leaveRequestId, String observation);

    boolean approveLeave(Long leaveRequestId, String note, UserRole currentUserRole);

    String rejectLeave(Long leaveRequestId, UserRole role, String reason, String observation);

    List<LeaveRequestDetailsDto> searchLeaveRequests(LeaveRequestSearchCriteria criteria);


}
