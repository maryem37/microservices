package tn.enis.DemandeConge.service.publicHoliday;

import tn.enis.DemandeConge.entity.PublicHoliday;

import java.time.LocalDate;
import java.util.List;

public interface PublicHolidayService {

    PublicHoliday addPublicHoliday(PublicHoliday publicHoliday);

    List<PublicHoliday> getAllPublicHolidays();

    PublicHoliday getPublicHolidayById(Long id);

    PublicHoliday updatePublicHoliday(Long id, PublicHoliday publicHoliday);

    void deletePublicHoliday(Long id);

    // Méthode utilitaire pour le calcul des congés
    boolean isHoliday(LocalDate date);
}
