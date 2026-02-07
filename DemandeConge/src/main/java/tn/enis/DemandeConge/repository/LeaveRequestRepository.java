package tn.enis.DemandeConge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;
import tn.enis.DemandeConge.enums.RequestState;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long>, JpaSpecificationExecutor<LeaveRequest> {

    // 4. Vérification chevauchement complexe (Jours + Heures)
    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l " +
            "WHERE l.userId = :userId " +
            "AND l.state NOT IN (:excludedStates) " +
            "AND (" +
            "   (l.fromDate <= :toDate AND l.toDate >= :fromDate) " +
            "   OR (l.fromDate = :fromDate AND l.periodType = :hourlyType AND :periodType = :hourlyType " +
            "       AND l.fromTime < :toTime AND l.toTime > :fromTime) " +
            ")")
    boolean existsOverlappingRequest(@Param("userId") Long userId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     @Param("fromTime") LocalTime fromTime,
                                     @Param("toTime") LocalTime toTime,
                                     @Param("periodType") PeriodType periodType,
                                     @Param("excludedStates") List<RequestState> excludedStates,
                                     @Param("hourlyType") PeriodType hourlyType);


    // 5. Demandes en attente (Pour calcul solde engagé)
    @Query("SELECT l FROM LeaveRequest l WHERE l.userId = :userId " +
            "AND l.state IN (tn.enis.DemandeConge.enums.RequestState.Pending, tn.enis.DemandeConge.enums.RequestState.InProgress)")
    List<LeaveRequest> findPendingRequests(@Param("userId") Long userId);

    // 6. Somme des heures d'autorisation du mois
    @Query("SELECT COALESCE(SUM(l.hours), 0) FROM LeaveRequest l " +
            "WHERE l.userId = :userId " +
            "AND l.type = :type " +
            "AND l.state NOT IN (:excludedStates) " +
            "AND MONTH(l.fromDate) = :month AND YEAR(l.fromDate) = :year")
    Double sumAuthorizationHoursThisMonth(
            @Param("userId") Long userId,
            @Param("type") LeaveType type,
            @Param("excludedStates") List<RequestState> excludedStates,
            @Param("month") int month,
            @Param("year") int year
    );}