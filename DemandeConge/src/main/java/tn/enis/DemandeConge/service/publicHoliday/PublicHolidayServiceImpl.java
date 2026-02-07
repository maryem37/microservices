package tn.enis.DemandeConge.service.publicHoliday;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.enis.DemandeConge.entity.PublicHoliday;
import tn.enis.DemandeConge.repository.PublicHolidayRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PublicHolidayServiceImpl implements PublicHolidayService {

    private final PublicHolidayRepository publicHolidayRepository;

    @Override
    public PublicHoliday addPublicHoliday(PublicHoliday publicHoliday) {
        // Validation simple : Date fin ne doit pas être avant date début
        if (publicHoliday.getEndDate() != null && publicHoliday.getStartDate().isAfter(publicHoliday.getEndDate())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }
        // Si endDate est null (jour férié d'un seul jour), on met la même date que startDate
        if (publicHoliday.getEndDate() == null) {
            publicHoliday.setEndDate(publicHoliday.getStartDate());
        }
        return publicHolidayRepository.save(publicHoliday);
    }

    @Override
    public List<PublicHoliday> getAllPublicHolidays() {
        return publicHolidayRepository.findAll();
    }

    @Override
    public PublicHoliday getPublicHolidayById(Long id) {
        return publicHolidayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jour férié introuvable avec l'ID : " + id));
    }

    @Override
    public PublicHoliday updatePublicHoliday(Long id, PublicHoliday updatedHoliday) {
        PublicHoliday existing = getPublicHolidayById(id);

        existing.setStartDate(updatedHoliday.getStartDate());
        existing.setEndDate(updatedHoliday.getEndDate());
        existing.setDescription(updatedHoliday.getDescription());

        // Re-validation des dates
        if (existing.getEndDate() != null && existing.getStartDate().isAfter(existing.getEndDate())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être avant la date de début.");
        }

        return publicHolidayRepository.save(existing);
    }

    @Override
    public void deletePublicHoliday(Long id) {
        if (!publicHolidayRepository.existsById(id)) {
            throw new RuntimeException("Jour férié introuvable avec l'ID : " + id);
        }
        publicHolidayRepository.deleteById(id);
    }

    @Override
    public boolean isHoliday(LocalDate date) {
        // Utilise la requête optimisée du repository
        return publicHolidayRepository.isDateHoliday(date);
    }
}