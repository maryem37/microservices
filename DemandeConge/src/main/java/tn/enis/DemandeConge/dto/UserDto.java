// src/main/java/tn/enis/DemandeConge/dto/UserDto.java
package tn.enis.DemandeConge.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String userRole; // optional
}
