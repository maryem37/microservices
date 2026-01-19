package tn.enis.DemandeConge.entity;

import jakarta.persistence.*;
import lombok.Data;
import tn.enis.DemandeConge.enums.RequestState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

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

    private Long userId;  // <-- store the user ID from the registration service
    private LocalDate cancellationDate;  // <-- date d'annulation
    private String cancellationObservation; // <-- observation facultative
}
