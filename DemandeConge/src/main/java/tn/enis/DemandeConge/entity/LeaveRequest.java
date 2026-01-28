package tn.enis.DemandeConge.entity;

import jakarta.persistence.*;
import lombok.Data;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.conge.enums.UserRole; // <- IMPORT IMPORTANT

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

    private Long days;

    @Enumerated(EnumType.STRING)
    private RequestState state;

    private String note;
    private String type;

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
}
