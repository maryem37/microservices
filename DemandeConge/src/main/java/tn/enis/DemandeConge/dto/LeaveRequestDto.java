package tn.enis.DemandeConge.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import tn.enis.DemandeConge.enums.RequestState;

import java.util.Date;

@Data
public class LeaveRequestDto {

    private Long id;

    private Date fromDate;

    private Date toDate;

    private Date fromTime;

    private Date toTime;

    private Long days;

    private RequestState state;

    private String note;
    private String type;
    private Long userId;


}
