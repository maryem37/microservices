package tn.enis.DemandeConge.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import tn.enis.DemandeConge.enums.RequestState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
public class LeaveRequestDto {

    private Long id;

    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalTime fromTime;
    private LocalTime toTime;

    private Long days;

    private RequestState state;

    private String note;
    private String type;
    private Long userId;


}
