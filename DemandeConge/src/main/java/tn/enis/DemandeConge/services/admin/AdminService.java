package tn.enis.DemandeConge.services.admin;

import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.conge.enums.UserRole;

import java.util.List;

public interface AdminService {

    // Nouvelle méthode pour accorder une demande
    List<LeaveRequestDto> getAllPendingLeaveRequests();
    boolean approveLeave(Long leaveRequestId, String note, UserRole currentUserRole);
    String rejectLeave(
            Long leaveRequestId,
            UserRole role,
            String reason,
            String observation
    );

    List<LeaveRequestDto> getAllLeaveRequests();

}
