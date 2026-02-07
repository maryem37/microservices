package tn.enis.DemandeConge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enis.DemandeConge.entity.PublicHoliday;

import java.time.LocalDate;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {

    // Cette méthode retourne TRUE si la date donnée est comprise dans un intervalle de jour férié
    @Query("SELECT COUNT(p) > 0 FROM PublicHoliday p WHERE :date BETWEEN p.startDate AND p.endDate")
    boolean isDateHoliday(@Param("date") LocalDate date);
}