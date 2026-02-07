package tn.enis.DemandeConge.utils;

import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class Preconditions {

    // 1. Vérification basique des objets nuls
    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    // 2. Vérification existence User
    public static void checkUserExists(Object userDto, Long userId) {
        if (userDto == null) {
            throw new RuntimeException("Utilisateur introuvable avec l'ID : " + userId);
        }
    }

    // 3. Cohérence des Dates
    public static void checkDateSequence(LocalDate fromDate, LocalDate toDate) {
        checkNotNull(fromDate, "La date de début est obligatoire.");
        if (toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("La Date Début (Du) ne peut pas être après la Date Fin (Au).");
        }
    }

    // 4. Cohérence des Heures (Spécifique aux congés par heure)
    public static void checkTimeSequence(PeriodType periodType, LocalTime fromTime, LocalTime toTime) {
        if (periodType == PeriodType.PAR_HEURE) {
            checkNotNull(fromTime, "L'heure de début est obligatoire pour un congé par heure.");
            checkNotNull(toTime, "L'heure de fin est obligatoire pour un congé par heure.");

            if (fromTime.isAfter(toTime)) {
                throw new IllegalArgumentException("L'heure de début doit être avant l'heure de fin.");
            }
        }
    }

    // 5. Délai de prévenance (Règle des 48h)
    public static void checkNoticePeriod(LocalDate fromDate, int hoursRequired) {
        if (fromDate == null) return;

        long hoursDiff = ChronoUnit.HOURS.between(LocalDateTime.now(), fromDate.atStartOfDay());
        if (hoursDiff < hoursRequired) {
            throw new IllegalArgumentException("Le délai de prévenance de " + hoursRequired + "h n'est pas respecté.");
        }
    }

    // 6. Validité de la durée (Résultat du calcul)
    public static void checkCalculatedDuration(double days, PeriodType periodType) {
        // Si ce n'est pas "par heure" et que le calcul donne 0 (ex: que des jours fériés)
        if (periodType != PeriodType.PAR_HEURE && days <= 0.0) {
            throw new RuntimeException("La durée du congé est nulle (Vérifiez si les dates correspondent à des jours fériés ou weekends).");
        }
    }

    // 7. Champs obligatoires du DTO
    public static void checkMandatoryFields(LeaveType type, PeriodType periodType) {
        checkNotNull(type, "Le type de congé est obligatoire.");
        checkNotNull(periodType, "Le type de période (Jour/Matin/Heure) est obligatoire.");
    }
}
