package tn.enis.DemandeConge.entity;

import jakarta.persistence.*;
import lombok.Data;
import tn.enis.DemandeConge.enums.RequestState;

import java.util.Date;

@Entity
@Data
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date fromDate;
    private Date toDate;

    private Date fromTime;
    private Date toTime;

    private Long days;

    @Enumerated(EnumType.STRING)
    private RequestState state;

    private String note;
    private String type;

    private Long userId;  // <-- store the user ID from the registration service
}
