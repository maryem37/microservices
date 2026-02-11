package tn.enis.conge.dto;


import lombok.Data;
import tn.enis.conge.enums.UserRole;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String numTel;
    private UserRole userRole;
    private Long departmentId;
    private String departmentName;
    // Pas de password ici pour la sécurité !
}
