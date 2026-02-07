package tn.enis.conge.dto;


import lombok.Data;
import tn.enis.conge.enums.UserRole;

@Data
public class SignupRequest {
    private String email;
    private String name;
//    private Integer numTel;
    private String password;
    private String cin;
    private Long departmentId;
    private UserRole userRole;

}
