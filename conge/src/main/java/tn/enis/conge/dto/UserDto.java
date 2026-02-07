package tn.enis.conge.dto;


import lombok.Data;
import tn.enis.conge.enums.UserRole;

@Data
public class UserDto {

    private Long id;
    private String name;
    private String email;
    private Integer numTel;
    private String password;
    private UserRole userRole;
    private Long departmentId;
    private String departmentName;
}
