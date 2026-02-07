package tn.enis.conge.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.conge.dto.DepartmentRequest;
import tn.enis.conge.entity.Department;
import tn.enis.conge.services.depart.DepartmentService;


import java.util.List;

@RestController
@RequestMapping("/api/admin/departments")
@RequiredArgsConstructor
public class DepartmentController {

    // Injection de l'interface (Spring trouvera tout seul l'implémentation)
    private final DepartmentService departmentService;

    @PostMapping("/create")
    public Department createDepartment(@RequestParam String name) {
        Department department = new Department();
        department.setNameDepartment(name);
        // L'ID est null ici, il sera généré par la DB
        // employeeCount n'est pas touché, il sera calculé par la @Formula
        return departmentService.createDepartment(department);
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, department));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok("Département supprimé avec succès.");
    }
}
