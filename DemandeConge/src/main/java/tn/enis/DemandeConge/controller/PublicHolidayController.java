package tn.enis.DemandeConge.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.enis.DemandeConge.entity.PublicHoliday;
import tn.enis.DemandeConge.service.publicHoliday.PublicHolidayService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/holidays") // Réservé aux admins
@RequiredArgsConstructor
public class PublicHolidayController {

    private final PublicHolidayService publicHolidayService;

    @PostMapping
    public ResponseEntity<PublicHoliday> addHoliday(@RequestBody PublicHoliday publicHoliday) {
        return ResponseEntity.ok(publicHolidayService.addPublicHoliday(publicHoliday));
    }

    @GetMapping
    public ResponseEntity<List<PublicHoliday>> getAllHolidays() {
        return ResponseEntity.ok(publicHolidayService.getAllPublicHolidays());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicHoliday> getHolidayById(@PathVariable Long id) {
        return ResponseEntity.ok(publicHolidayService.getPublicHolidayById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicHoliday> updateHoliday(@PathVariable Long id, @RequestBody PublicHoliday publicHoliday) {
        return ResponseEntity.ok(publicHolidayService.updatePublicHoliday(id, publicHoliday));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHoliday(@PathVariable Long id) {
        publicHolidayService.deletePublicHoliday(id);
        return ResponseEntity.ok("Jour férié supprimé avec succès.");
    }
}
