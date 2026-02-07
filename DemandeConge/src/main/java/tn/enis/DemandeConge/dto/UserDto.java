// src/main/java/tn/enis/DemandeConge/dto/UserDto.java
package tn.enis.DemandeConge.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String lastName;
    private String firstName;
    private String email;
    private String userRole; // optional
    // --- Aplatissement du département ---
    private Long departmentId;      // Pour faire des liens si besoin
    private String departmentName;

}
