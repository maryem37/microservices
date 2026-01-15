package tn.enis.conge.dto;


import lombok.Data;

@Data
public class SignupRequest {
    private String email;
    private String name;
//    private Integer numTel;
    private String password;
    private String cin;
}
