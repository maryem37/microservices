package tn.enis.conge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ChangePasswordAfterReset {
    @Id
    private String email;
    @Column(nullable = true)
    private String temporaryPassword;
    @Column(nullable = true)
    private String newPassword;

}

