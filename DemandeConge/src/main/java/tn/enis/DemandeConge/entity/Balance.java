package tn.enis.DemandeConge.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.enis.conge.entity.User;

@Entity
@Data
@NoArgsConstructor
public class Balance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double annualBalance;
    private double recoveryBalance;

    private Long userId;
}
