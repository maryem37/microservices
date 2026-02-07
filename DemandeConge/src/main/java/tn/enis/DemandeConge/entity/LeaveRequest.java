package tn.enis.DemandeConge.entity;

import jakarta.persistence.*;
import lombok.Data;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.conge.enums.UserRole;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fromDate;
    private LocalDate toDate;

    private LocalTime fromTime;
    private LocalTime toTime;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;
    private Long days;
    private Double hours;
    @Enumerated(EnumType.STRING)
    private RequestState state;

    private String note;

    @Enumerated(EnumType.STRING)
    private LeaveType type;

    private Long userId;  // store the user ID from the registration service
    private LocalDate cancellationDate;  // date d'annulation
    private String cancellationObservation; // observation facultative

    // ================================================
    // ⚡ NOUVEAUX CHAMPS POUR LA CHAINE DE VALIDATION
    // ================================================

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<UserRole, Boolean> approvalChain = new HashMap<>();
    // clé = rôle (TeamLeader, Administration)
    // valeur = true si le rôle a validé, false sinon

    private String managerNote; // note ajoutée par TeamLeader ou Admin
    // =====================
// REFUS
// =====================
    @Enumerated(EnumType.STRING)
    private UserRole rejectedBy;

    private LocalDate rejectionDate;
    private String rejectionReason;
    private String rejectionObservation;

}
