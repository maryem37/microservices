package tn.enis.conge.services.depart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enis.conge.entity.Department;
import tn.enis.conge.repository.DepartmentRepository;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional // Gère les transactions automatiquement (rollback en cas d'erreur)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department createDepartment(Department department) {
        if (departmentRepository.existsByNameDepartment(department.getNameDepartment())) {
            throw new RuntimeException("Département déjà existant");
        }
        // Hibernate va générer l'INSERT avec seulement le nom
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartments() {
        // Grâce à @Formula dans l'entité, le employeeCount sera calculé ici
        return departmentRepository.findAll();
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département introuvable avec l'ID : " + id));
    }

    @Override
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department existingDept = getDepartmentById(id);

        // On met à jour uniquement le nom (l'ID et le count ne changent pas manuellement)
        existingDept.setNameDepartment(departmentDetails.getNameDepartment());

        return departmentRepository.save(existingDept);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department existingDept = getDepartmentById(id);

        // Règle métier de sécurité :
        // On interdit la suppression si des employés sont liés à ce département
        if (existingDept.getUsers() != null && !existingDept.getUsers().isEmpty()) {
            throw new RuntimeException("Impossible de supprimer : le département contient "
                    + existingDept.getUsers().size() + " employés. Déplacez-les d'abord.");
        }

        departmentRepository.delete(existingDept);
    }
}
