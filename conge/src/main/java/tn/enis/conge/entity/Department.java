package tn.enis.conge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameDepartment;
    @Formula("(SELECT COUNT(*) FROM users u WHERE u.department_id = id)")
    private int employeeCount;

    @OneToMany(mappedBy = "department")
    @JsonIgnore
    private List<User> users;



}