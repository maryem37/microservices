package tn.enis.DemandeConge.dto;

import lombok.Builder;
import lombok.Data;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.conge.enums.UserRole;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LeaveRequestSearchCriteria {
    private Long currentUserId;
    private String firstName;
    private String lastName;
    private String cin;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String deptName;
    private LeaveType type;
    private List<RequestState> states;
    private UserRole validationLevel;
}
