package tn.enis.conge.dto;

import lombok.Data;
import tn.enis.conge.enums.UserRole;

@Data
public class AuthenticationResponse {

private String jwt;
private UserRole userRole;
private Long userId;

}

