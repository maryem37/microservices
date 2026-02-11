package tn.enis.conge.dto;


import lombok.Data;
import tn.enis.conge.enums.UserRole;

@Data
public class SignupRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String cin;
    private String numTel;
    private String password;  // L'admin saisira un mot de passe initial
    private Long departmentId;
    private UserRole userRole;
}
