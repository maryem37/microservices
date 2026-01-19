package tn.enis.DemandeConge.services.EmployeReq;

import tn.enis.DemandeConge.dto.LeaveRequestDto;

public interface EmployerReq {

    boolean leaveRequest(LeaveRequestDto leaveRequestDto);
    String cancelLeaveRequest(Long leaveRequestId, String observation);

}
