package tn.enis.DemandeConge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;
import tn.enis.DemandeConge.enums.RequestState;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDetailsDto {

    // --- Données du Congé ---
    private Long id;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;
    private LeaveType type;
    private PeriodType periodType;
    private Long days;
    private Double hours;
    private RequestState state;

    // --- Données de l'Utilisateur  ---
    private Long userId;
    private String lastName;
    private String firstName;
    private String role;
    private String departement;
}