package tn.enis.DemandeConge.services.admin;

import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.conge.enums.UserRole;

import java.util.List;

public interface AdminService {

    // Nouvelle méthode pour accorder une demande
    List<LeaveRequestDto> getAllPendingLeaveRequests();
    boolean approveLeave(Long leaveRequestId, String note, UserRole currentUserRole);

    List<LeaveRequestDto> getAllLeaveRequests();

}
