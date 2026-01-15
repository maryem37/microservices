package tn.enis.conge.dto;


import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}
