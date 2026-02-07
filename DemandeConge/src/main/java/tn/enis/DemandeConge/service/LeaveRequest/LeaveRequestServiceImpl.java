package tn.enis.DemandeConge.service.LeaveRequest;

import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.enis.DemandeConge.client.RegistrationClient;
import tn.enis.DemandeConge.dto.LeaveRequestDetailsDto;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.dto.LeaveRequestSearchCriteria;
import tn.enis.DemandeConge.dto.UserDto;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.LeaveType;
import tn.enis.DemandeConge.enums.PeriodType;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.repository.BalanceRepository;
import tn.enis.DemandeConge.repository.LeaveRequestRepository;
import tn.enis.DemandeConge.repository.PublicHolidayRepository;
import tn.enis.DemandeConge.service.balance.BalanceService;
import tn.enis.DemandeConge.utils.Preconditions;
import tn.enis.conge.enums.UserRole;
import org.springframework.data.jpa.domain.Specification;
import tn.enis.DemandeConge.entity.Balance;

import java.time.LocalTime;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j  // <--- 2. C'EST CETTE ANNOTATION QUI CRÉE LA VARIABLE 'log'
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final RegistrationClient registrationClient;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PublicHolidayRepository publicHolidayRepository;
    private final BalanceRepository    balanceRepository;
    private final BalanceService balanceService;
    /**
     *  Ajout demande congée
     */

    @Override
    public LeaveRequest createLeaveRequest(LeaveRequestDto dto) {

        // --- A. VALIDATIONS PRÉLIMINAIRES (Preconditions) ---
        Preconditions.checkMandatoryFields(dto.getType(), dto.getPeriodType());

        checkTypeAndPeriodConsistency(dto.getType(), dto.getPeriodType());
        Preconditions.checkDateSequence(dto.getFromDate(), dto.getToDate());
        Preconditions.checkTimeSequence(dto.getPeriodType(), dto.getFromTime(), dto.getToTime());

        // --- B. RÉCUPÉRATION USER ---
        UserDto userDto = null;
        try {
            userDto = registrationClient.getUserById(dto.getUserId());
        } catch (Exception e) { log.error("Erreur Auth", e); }
        Preconditions.checkUserExists(userDto, dto.getUserId());

        // --- C. CALCUL DES DURÉES (Jours ou Heures) ---
        double calculatedDays = 0.0;
        double calculatedHours = 0.0;

        if (dto.getPeriodType() == PeriodType.PAR_HEURE) {
            // On vérifie tout de suite si l'heure demandée est valide (8-12 ou 13-17)
            checkWorkingHours(dto.getFromTime(), dto.getToTime());
            // 1. Vérification si le jour est férié ou weekend
            boolean isHoliday = publicHolidayRepository.isDateHoliday(dto.getFromDate());
            boolean isWeekend = dto.getFromDate().getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                    dto.getFromDate().getDayOfWeek() == java.time.DayOfWeek.SUNDAY;

            if (isHoliday || isWeekend) {
                // Si c'est férié/weekend, la durée est 0 (la demande est invalide pour un congé payé)
                calculatedHours = 0.0;
            } else {
                // Calcul de la différence d'heures
                long minutes = java.time.Duration.between(dto.getFromTime(), dto.getToTime()).toMinutes();
                calculatedHours = minutes / 60.0;
            }

            // On vérifie que le calcul n'est pas 0 (Exception implicite)
            if (calculatedHours <= 0) {
                throw new RuntimeException("La durée calculée en heures est nulle ou invalide (Jour férié/Weekend).");
            }
            // On vérifie que le calcul n'est pas supp à 3h (Exception implicite)
            if (calculatedHours > 3) {
                throw new RuntimeException("La durée calculée en heures est suppérieur à 3h ou invalide (Jour férié/Weekend).");
            }

        } else {
            // Calcul par jours
            calculatedDays = calculateBusinessDays(dto.getFromDate(), dto.getToDate(), dto.getPeriodType());
            Preconditions.checkCalculatedDuration(calculatedDays, dto.getPeriodType());
        }

        // Définir les états à ignorer (Rejected, Cancelled) pour le chevauchement
        List<RequestState> nonOverlappingStates = List.of(RequestState.Rejected, RequestState.Cancelled);

        // --- D. VÉRIFICATION CHEVAUCHEMENT  ---
        boolean overlap = leaveRequestRepository.existsOverlappingRequest(
                userDto.getId(),
                dto.getFromDate(),
                dto.getToDate(),
                dto.getFromTime(),
                dto.getToTime(),
                dto.getPeriodType(),
                nonOverlappingStates,
                PeriodType.PAR_HEURE
        );

        if (overlap) {
            throw new RuntimeException("A leave request already exists for this period (Pending or Approved)..");
        }


        // --- E. VÉRIFICATION DU SOLDE  ---
        // On vérifie le solde en tenant compte des demandes en attente
        checkBalanceAvailability(userDto.getId(), dto.getType(), calculatedDays, calculatedHours, dto.getFromDate());


        // --- F. CRÉATION ET ENREGISTREMENT  ---
        tn.enis.DemandeConge.entity.LeaveRequest request = new tn.enis.DemandeConge.entity.LeaveRequest();

        // 1. Génération données de base
        request.setUserId(userDto.getId());
        request.setType(dto.getType());
        request.setPeriodType(dto.getPeriodType());
        request.setFromDate(dto.getFromDate());
        request.setToDate(dto.getToDate());
        request.setFromTime(dto.getFromTime());
        request.setToTime(dto.getToTime());
        request.setNote(dto.getNote());

        // 2. Initialisation État  -> "En attente" géré par initializeWorkflow
        UserRole userRole = UserRole.valueOf(userDto.getUserRole());
        initializeWorkflow(request, userRole);

        // A ce stade : request.getDays() est NULL et request.getHours() est NULL par défaut.

        // 2. Gestion spécifique (On set UNIQUEMENT ce qui est nécessaire)
        if (dto.getType() == LeaveType.AUTHORIZED_ABSENCE || dto.getType() == LeaveType.RECOVERY_LEAVE ) {
            // Cas : Heures (Autorisation ou Récupération)
            request.setHours(calculatedHours);
            // Pas besoin de setDays(null), il l'est déjà.
        } else {
            // Cas : Jours (Annuel, Maladie, etc.)
            request.setDays((long) Math.ceil(calculatedDays));
            // Pas besoin de setHours(null), il l'est déjà.
        }

        // 5. Sauvegarde (Step h.10)
        return leaveRequestRepository.save(request);
    }

    // ---- Validation de cohérence Type vs Période ---
    private void checkTypeAndPeriodConsistency(LeaveType type, PeriodType period) {

        // CAS 1 : Les types qui DOIVENT être PAR HEURE
        if (type == LeaveType.RECOVERY_LEAVE || type == LeaveType.AUTHORIZED_ABSENCE) {
            if (period != PeriodType.PAR_HEURE) {
                throw new RuntimeException("Leave type '" + type + "' must be requested by HOURS (PeriodType = PAR_HEURE).");
            }
        }

        // CAS 2 : Les types qui DOIVENT être PAR JOUR (ou Demi-journée)
        else if (type == LeaveType.ANNUAL_LEAVE || type == LeaveType.UNPAID_LEAVE) {
            // On vérifie que la période est soit Journée complète, soit Matin, soit Après-midi
            boolean isValidPeriod = (period == PeriodType.JOURNEE_COMPLETE ||
                    period == PeriodType.MATIN ||
                    period == PeriodType.APRES_MIDI);

            if (!isValidPeriod) {
                throw new RuntimeException("Leave type '" + type + "' can only be requested by Full Day or Half Day.");
            }
        }
    }


    // -- Configure la chaîne d'approbation et l'état initial selon le rôle

    private void initializeWorkflow(tn.enis.DemandeConge.entity.LeaveRequest request, UserRole userRole) {
        Map<UserRole, Boolean> chain = new HashMap<>();

        switch (userRole) {
            case Employer:
                // L'employé a besoin de validation Chef + Admin
                chain.put(UserRole.TeamLeader, false);    // Étape 1 : Non validé
                chain.put(UserRole.Administration, false); // Étape 2 : Non validé

                request.setState(RequestState.Pending); // État initial
                break;

            case TeamLeader:
                // Le chef a besoin uniquement de validation Admin
                chain.put(UserRole.Administration, false); // Étape 1 : Non validé

                request.setState(RequestState.InProgress); // État initial (Saute l'étape chef)
                break;

            case Administration:
                // L'admin s'auto-valide (ou est validé d'office)
                chain.put(UserRole.Administration, true);

                request.setState(RequestState.Approved); // Directement validé
                break;

            default:
                throw new RuntimeException("Rôle utilisateur non géré pour la demande de congé");
        }

        request.setApprovalChain(chain);
    }
    // --- MÉTHODE PRIVÉE : VÉRIFICATION HORAIRES DE TRAVAIL ---
    private void checkWorkingHours(LocalTime from, LocalTime to) {
        // Définition des bornes (Tu peux aussi les mettre en constantes statiques)
        LocalTime startMorning = LocalTime.of(8, 0);
        LocalTime endMorning = LocalTime.of(12, 0);

        LocalTime startAfternoon = LocalTime.of(13, 0);
        LocalTime endAfternoon = LocalTime.of(17, 0);

        // Vérifie si la période est incluse dans la matinée
        // (!isBefore = est après ou égal, !isAfter = est avant ou égal)
        boolean isMorning = !from.isBefore(startMorning) && !to.isAfter(endMorning);

        // Vérifie si la période est incluse dans l'après-midi
        boolean isAfternoon = !from.isBefore(startAfternoon) && !to.isAfter(endAfternoon);

        // Si ça ne rentre ni dans le matin, ni dans l'après-midi, on rejette
        if (!isMorning && !isAfternoon) {
            throw new RuntimeException("Requested hours (" + from + " - " + to + ") must be within working hours: 08:00-12:00 OR 13:00-17:00 (Lunch break excluded).");
        }
    }

    // --- MÉTHODE PRIVÉE : VÉRIFICATION SOLDE  ---
    private void checkBalanceAvailability(Long userId, LeaveType type, double requestedDays, double requestedHours, LocalDate refDate) {

        // 1. Récupérer le solde actuel
        Balance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No balance found for this user."));

        // 2. Récupérer les demandes en attente pour calculer le "Solde Engagé"
        List<tn.enis.DemandeConge.entity.LeaveRequest> pendingRequests = leaveRequestRepository.findPendingRequests(userId);

        if (type == LeaveType.ANNUAL_LEAVE) {
            // Somme des jours en attente
            double pendingDays = pendingRequests.stream()
                    .filter(r -> r.getType() == LeaveType.ANNUAL_LEAVE && r.getDays() != null)
                    .mapToDouble(tn.enis.DemandeConge.entity.LeaveRequest::getDays)
                    .sum();

            double totalNeeded = pendingDays + requestedDays;

            if (balance.getAnnualBalance() < totalNeeded) {
                throw new RuntimeException("Insufficient Annual Balance (Balance: " + balance.getAnnualBalance() + ", Requested + Pending: " + totalNeeded + ")");
            }

        } else if (type == LeaveType.RECOVERY_LEAVE) {
            // Somme des heures en attente
            double pendingHours = pendingRequests.stream()
                    .filter(r -> r.getType() == LeaveType.RECOVERY_LEAVE && r.getHours() != null)
                    .mapToDouble(tn.enis.DemandeConge.entity.LeaveRequest::getHours)
                    .sum();

            double totalNeeded = pendingHours + requestedHours;

            // On suppose que le solde récupération est stocké en heures
            // Si recoveryBalance est en jours, il faut convertir (ex: * 8)
            if (balance.getRecoveryBalance() < totalNeeded) {
                throw new RuntimeException("Insufficient Recovery Balance (Balance: " + balance.getRecoveryBalance() + ", Requested + Pending: " + totalNeeded + ")");
            }

        } else if (type == LeaveType.AUTHORIZED_ABSENCE) {
            // Vérification du quota du mois (3 heures max par mois)
            // On récupère ce qui a déjà été consommé ce mois-ci (Validé + En attente)

            // On définit les états à ignorer (Rejeté et Annulé)
            List<RequestState> excludedStates = List.of(RequestState.Rejected, RequestState.Cancelled);

            Double hoursUsedThisMonth = leaveRequestRepository.sumAuthorizationHoursThisMonth(userId,
                    LeaveType.AUTHORIZED_ABSENCE,
                    excludedStates,
                    refDate.getMonthValue(),
                    refDate.getYear());

            // Ajout de la demande actuelle
            double totalMonthUsage = hoursUsedThisMonth + requestedHours;

            // Récupérer la limite autorisée (Soit dans Balance, soit une constante, ex: 4.0 heures)
            // Supposons que 'balance.getAuthorizationBalance()' retourne le quota restant ou total.
            // Si c'est une règle fixe (ex: 4h/mois) :
            double monthlyLimit = 3.0;

            if (totalMonthUsage > monthlyLimit) {
                throw new RuntimeException("Monthly Authorization limit exceeded (Used: " + hoursUsedThisMonth + "h, Requested: " + requestedHours + "h, Limit: " + monthlyLimit + "h)");
            }
        }
    }
    // -- Méthode principale de calcul
    private double calculateBusinessDays(LocalDate start, LocalDate end, PeriodType periodType) {

        // CAS 1 : Demi-journée (Matin ou Après-midi)
        // On considère que start == end pour une demi-journée
        if (periodType == PeriodType.MATIN || periodType == PeriodType.APRES_MIDI) {
            // Si le jour choisi est un weekend ou un jour férié, ça compte 0
            if (isWeekend(start) || publicHolidayRepository.isDateHoliday(start)) {
                return 0.0;
            }
            return 0.5; // Une demi-journée valide vaut 0.5
        }

        // CAS 2 : Journée complète (Période)
        double businessDays = 0;
        LocalDate current = start;

        // On boucle tant que 'current' n'a pas dépassé la date de fin
        while (!current.isAfter(end)) {

            boolean isWeekend = isWeekend(current);
            // Appel à la base de données pour vérifier si ce jour précis est férié
            boolean isHoliday = publicHolidayRepository.isDateHoliday(current);

            // On incrémente le compteur SEULEMENT si c'est un jour travaillé
            if (!isWeekend && !isHoliday) {
                businessDays++;
            }

            // On passe au jour suivant
            current = current.plusDays(1);
        }

        return businessDays;
    }

    // -- Petite méthode utilitaire pour la lisibilité
    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }

    /**
     * Accepter Demande congée
     */
    @Override
    @Transactional
    public boolean approveLeave(Long leaveRequestId, String note, UserRole currentUserRole) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // Check the role chain
        if (!canApprove(currentUserRole, leaveRequest)) {
            throw new RuntimeException("This exceeds your validation authority");
        }

        // Check the current state (Pending or InProgress only)
        if (!(leaveRequest.getState() == RequestState.Pending || leaveRequest.getState() == RequestState.InProgress)) {
            throw new RuntimeException("Validation failed");
        }

        // Check if already approved by this role
        if (leaveRequest.getApprovalChain().getOrDefault(currentUserRole, false)) {
            throw new RuntimeException("Leave request already approved by this user");
        }

        // Approve the request for this role
        leaveRequest.getApprovalChain().put(currentUserRole, true);
        if (note != null && !note.isEmpty()) leaveRequest.setManagerNote(note);

        // Determine new state
        boolean allApproved = leaveRequest.getApprovalChain().values().stream().allMatch(Boolean::booleanValue);
        if (allApproved) {
            leaveRequest.setState(RequestState.Approved);  // Only final approver sets Approved
            // 2. Appel au BalanceService pour débiter
            // On passe les infos nécessaires : UserId, Type, Jours (peut être null), Heures (peut être null)
            // On utilise des valeurs par défaut 0.0 si c'est null pour éviter les NullPointerException
            Double daysToDeduct = leaveRequest.getDays() != null ? (double) leaveRequest.getDays() : 0.0;
            Double hoursToDeduct = leaveRequest.getHours() != null ? leaveRequest.getHours() : 0.0;

            balanceService.debitBalance(
                    leaveRequest.getUserId(),
                    leaveRequest.getType(),
                    daysToDeduct,
                    hoursToDeduct
            );
        } else {
            leaveRequest.setState(RequestState.InProgress); // Otherwise InProgress
        }

        leaveRequestRepository.save(leaveRequest);
        return true;
    }

    private boolean canApprove(UserRole role, LeaveRequest leaveRequest) {
        if (role == UserRole.TeamLeader) {
            // TeamLeader peut approuver seulement s'il n'a pas encore approuvé
            return leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false) == false;
        }
        if (role == UserRole.Administration) {
            // Administration peut approuver seulement si TeamLeader a déjà approuvé
            return leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false) == true
                    && leaveRequest.getApprovalChain().getOrDefault(UserRole.Administration, false) == false;
        }
        return false;
    }

    /**
     * Refuser Demande congée
     */

    @Override
    public String rejectLeave(
            Long leaveRequestId,
            UserRole role,
            String reason,
            String observation
    ) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        // ❌ Exception 23: reason is mandatory
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Exception 23: A rejection reason must be provided!");
        }

        // ❌ Exception 24: already rejected
        if (leaveRequest.getState() == RequestState.Rejected) {
            throw new RuntimeException("Exception 24: Leave request has already been rejected");
        }

        // ❌ Exception 25: already approved or cancelled
        if (leaveRequest.getState() == RequestState.Approved ||
                leaveRequest.getState() == RequestState.Cancelled) {
            throw new RuntimeException("Exception 25: Leave request is already approved or cancelled");
        }

        // ❌ Exception 17: respect approval chain
        if (role == UserRole.Administration &&
                !leaveRequest.getApprovalChain().getOrDefault(UserRole.TeamLeader, false)) {
            throw new RuntimeException("Exception 17: Waiting for previous approval in the chain");
        }

        // ❌ Exception 19: already processed by this user
        if (leaveRequest.getApprovalChain().getOrDefault(role, false)) {
            throw new RuntimeException("Exception 19: This request has already been processed by this user");
        }

        // ✅ FINAL REJECTION
        leaveRequest.setState(RequestState.Rejected);
        leaveRequest.setRejectedBy(role);
        leaveRequest.setRejectionDate(LocalDate.now());
        leaveRequest.setRejectionReason(reason);
        leaveRequest.setRejectionObservation(observation);

        leaveRequestRepository.save(leaveRequest);

        return "Leave request rejected successfully";
    }

    /**
     * Annuler Demande congée
     */
    @Override
    public String cancelLeaveRequest(Long leaveRequestId, String observation) {
        try {
            // 1️⃣ Get the leave request from the DB
            tn.enis.DemandeConge.entity.LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                    .orElse(null);

            if (leaveRequest == null) {
                return "Leave request not found.";
            }

            // 2️⃣ Check the state of the leave request
            if (leaveRequest.getState() == RequestState.Approved) {
                return "This leave request has already been approved.";
            }
            if (leaveRequest.getState() == RequestState.Rejected) {
                return "This leave request has already been rejected.";
            }
            if (leaveRequest.getState() == RequestState.Cancelled) {
                return "This leave request has already been cancelled and cannot be processed.";
            }
            if (leaveRequest.getToDate().isBefore(java.time.LocalDate.now())) {
                return "Action impossible: the period for this leave request has already passed.";
            }

            // 3️⃣ Cancel the leave request
            leaveRequest.setState(RequestState.Cancelled);
            leaveRequest.setCancellationDate(java.time.LocalDate.now());
            leaveRequest.setCancellationObservation(observation);

            leaveRequestRepository.save(leaveRequest);

            return "Leave request cancelled successfully.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while cancelling the leave request.";
        }
    }

    /**
     * Afficher Demande congée
     */
    @Override
    public List<LeaveRequestDetailsDto> searchLeaveRequests(LeaveRequestSearchCriteria criteria) {

        // 1. Validation des préconditions
        if (criteria.getFromDate() != null && criteria.getToDate() != null) {
            Preconditions.checkDateSequence(criteria.getFromDate(), criteria.getToDate());
        }
        // 2. Récupération de l'utilisateur courant
        UserDto currentUser = getCurrentUser(criteria.getCurrentUserId());

        // 3. Construction et exécution de la recherche
        List<LeaveRequest> requests =leaveRequestRepository.findAll(buildSearchSpecification(currentUser, criteria));

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

             return requests.stream()
                .map(this::enrichWithUser) // Appel de la méthode d'aide ci-dessous
                .toList();
    }
    /**
     * Méthode d'aide pour récupérer le User et créer le DTO
     */
    private LeaveRequestDetailsDto enrichWithUser(LeaveRequest request) {
        UserDto user = null;

        // On tente de récupérer l'utilisateur via Feign (GET /api/users/{id})
        try {
            if (request.getUserId() != null) {
                user = registrationClient.getUserById(request.getUserId());
            }
        } catch (Exception e) {
            // En cas d'erreur (User supprimé ou Microservice éteint), on log juste l'erreur
            // On ne bloque pas tout le processus, l'user restera null
            System.err.println("Erreur lors de la récupération de l'user ID " + request.getUserId() + ": " + e.getMessage());
        }

        // On convertit et on retourne
        return convertToDetailsDto(request, user);
    }

    private LeaveRequestDetailsDto convertToDetailsDto(LeaveRequest request, UserDto user) {

        LeaveRequestDetailsDto.LeaveRequestDetailsDtoBuilder builder = LeaveRequestDetailsDto.builder()
                // Données locales (LeaveRequest)
                .id(request.getId())
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .fromTime(request.getFromTime())
                .toTime(request.getToTime())
                .periodType(request.getPeriodType())
                .type(request.getType())
                .days(request.getDays())
                .hours(request.getHours())
                .state(request.getState())
                .userId(request.getUserId());

        // Données distantes (UserDto)
        if (user != null) {
            builder.lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .role(user.getUserRole())
                    .departement(user.getDepartmentName());
        } else {
            // Gestion du cas où l'utilisateur n'est pas trouvé (ex: supprimé)
            builder.lastName("Utilisateur Inconnu")
                    .departement("N/A");
        }

        return builder.build();
    }

     // -- Récupère et valide l'utilisateur courant

    private UserDto getCurrentUser(Long currentUserId) {
        UserDto currentUser = registrationClient.getUserById(currentUserId);
        Preconditions.checkUserExists(currentUser, currentUserId);
        return currentUser;
    }

    /**
     * Construit la spécification complète de recherche
     */
    private Specification<tn.enis.DemandeConge.entity.LeaveRequest> buildSearchSpecification(
            UserDto currentUser, LeaveRequestSearchCriteria criteria) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // A. Logique de visibilité (qui peut voir quoi)
            addVisibilityPredicates(predicates, root, cb, currentUser, criteria.getCurrentUserId());

            // B. Logique des états
            addStatePredicates(predicates, root, criteria.getStates());

            // C. Filtres optionnels
            addOptionalFilters(predicates, root, cb, currentUser, criteria);

            // D. Tri de la liste
            applySorting(query, cb, root);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Ajoute les prédicats de visibilité selon le rôle
     */
    private void addVisibilityPredicates(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, UserDto currentUser, Long currentUserId) {

        String userRole = currentUser.getUserRole();

        if (UserRole.Employer.name().equals(userRole)) {
            // L'employé ne voit que ses propres demandes
            predicates.add(cb.equal(root.get("userId"), currentUserId));
        }
        else if (UserRole.TeamLeader.name().equals(userRole)) {
            // Le TeamLeader voit son département
            addTeamLeaderVisibilityPredicate(predicates, root, cb, currentUser);
        }
        // L'Administration voit tout (pas de prédicat)
    }

    /**
     * Ajoute le prédicat de visibilité spécifique au TeamLeader
     */
    private void addTeamLeaderVisibilityPredicate(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, UserDto currentUser) {

        List<Long> memberIds = registrationClient.searchUserIds(
                null, null, null, currentUser.getDepartmentName());

        if (memberIds != null && !memberIds.isEmpty()) {
            predicates.add(root.get("userId").in(memberIds));
        } else {
            // Département vide : aucun résultat
            predicates.add(cb.disjunction());
        }
    }

    /**
     * Ajoute les prédicats pour les états de demande
     */
    private void addStatePredicates(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            List<RequestState> states) {

        if (states != null && !states.isEmpty()) {
            // États sélectionnés par l'utilisateur
            predicates.add(root.get("state").in(states));
        } else {
            // Par défaut : Pending et InProgress uniquement
            predicates.add(root.get("state").in(
                    RequestState.Pending, RequestState.InProgress));
        }
    }

    /**
     * Ajoute tous les filtres optionnels
     */
    private void addOptionalFilters(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, UserDto currentUser,
            LeaveRequestSearchCriteria criteria) {

        // 1. Filtre par type de congé
        addLeaveTypeFilter(predicates, root, cb, criteria.getType());

        // 2. Filtres de dates
        addDateFilters(predicates, root, cb, criteria.getFromDate(), criteria.getToDate());

        // 3. Recherche par nom/prénom/CIN
        addUserSearchFilter(predicates, root, cb,
                criteria.getFirstName(), criteria.getLastName(), criteria.getCin());

        // 4. Recherche par département
        addDepartmentFilter(predicates, root, cb, currentUser, criteria.getDeptName());

        // 5. Recherche par niveau de validation
        addValidationLevelFilter(predicates, root, currentUser, criteria.getValidationLevel(), cb);
    }

    /**
     * Filtre par type de congé
     */
    private void addLeaveTypeFilter(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, LeaveType type) {

        if (type != null) {
            predicates.add(cb.equal(root.get("type"), type));
        }
    }

    /**
     * Filtres de dates (du/au)
     */
    private void addDateFilters(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, LocalDate fromDate, LocalDate toDate) {

        // À partir de cette date
        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fromDate"), fromDate));
        }

        // Jusqu'à cette date
        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("toDate"), toDate));
        }
    }

    /**
     * Filtre par nom, prénom ou CIN
     */
    private void addUserSearchFilter(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, String firstName, String lastName, String cin) {

        if (isAnyUserSearchCriteriaProvided(firstName, lastName, cin)) {
            List<Long> filteredUserIds = registrationClient.searchUserIds(
                    firstName, lastName, cin, null);

            if (filteredUserIds != null && !filteredUserIds.isEmpty()) {
                predicates.add(root.get("userId").in(filteredUserIds));
            } else {
                predicates.add(cb.disjunction()); // Aucun résultat
            }
        }
    }

    /**
     * Vérifie si au moins un critère de recherche utilisateur est fourni
     */
    private boolean isAnyUserSearchCriteriaProvided(
            String firstName, String lastName, String cin) {

        return (firstName != null && !firstName.isEmpty()) ||
                (lastName != null && !lastName.isEmpty()) ||
                (cin != null && !cin.isEmpty());
    }

    /**
     * Filtre par département (interdit aux employés)
     */
    private void addDepartmentFilter(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            CriteriaBuilder cb, UserDto currentUser, String deptName) {

        // Les employés ne peuvent pas filtrer par département
        if (UserRole.Employer.name().equals(currentUser.getUserRole())) {
            return;
        }

        if (deptName != null && !deptName.isEmpty()) {
            List<Long> deptUserIds = registrationClient.searchUserIds(
                    null, null, null, deptName);

            if (deptUserIds != null && !deptUserIds.isEmpty()) {
                predicates.add(root.get("userId").in(deptUserIds));
            } else {
                predicates.add(cb.disjunction());
            }
        }
    }

    /**
     * Filtre par niveau de validation (Admin/TeamLeader seulement)
     */
    private void addValidationLevelFilter(
            List<Predicate> predicates, Root<tn.enis.DemandeConge.entity.LeaveRequest> root,
            UserDto currentUser, UserRole validationLevel, CriteriaBuilder cb) {


        // Les employés ne peuvent pas filtrer par niveau de validation
        if (UserRole.Employer.name().equals(currentUser.getUserRole())) {
            return;
        }

        if (validationLevel != null) {
            MapJoin<tn.enis.DemandeConge.entity.LeaveRequest, UserRole, Boolean> approvalJoin =
                    root.joinMap("approvalChain");
            predicates.add(cb.equal(approvalJoin.key(), validationLevel));
            predicates.add(cb.equal(approvalJoin.value(), false));
        }
    }

    /**
     * Applique le tri sur les résultats
     */
    private void applySorting(
            CriteriaQuery<?> query, CriteriaBuilder cb,
            Root<tn.enis.DemandeConge.entity.LeaveRequest> root) {

        query.orderBy(
                cb.asc(root.get("state")),           // État (croissant)
                cb.desc(root.get("fromDate")),       // Date de début (décroissant)
                cb.desc(root.get("id"))              // ID (décroissant)
        );
    }

}