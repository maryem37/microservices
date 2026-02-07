package tn.enis.DemandeConge.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;
import tn.enis.DemandeConge.enums.RequestState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

@Data
public class LeaveRequestDto {

    private Long id;

    private LocalDate fromDate;
    private LocalDate toDate;
    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(type = "string", format = "time", description = "Heure de début (HH:mm:ss)")
    private LocalTime fromTime;
    @Schema(type = "string", format = "time",  description = "Heure de début (HH:mm:ss)")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime toTime;
    private PeriodType periodType;
    private String note;
    private LeaveType type;
    private Long userId;


}
