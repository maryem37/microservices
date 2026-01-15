package tn.enis.DemandeConge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.RequestState;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findAllByState(RequestState state);
}
