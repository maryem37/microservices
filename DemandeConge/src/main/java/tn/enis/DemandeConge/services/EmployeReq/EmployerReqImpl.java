package tn.enis.DemandeConge.services.EmployeReq;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enis.DemandeConge.client.RegistrationClient;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.dto.UserDto;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.repository.LeaveRequestRepository;
import feign.FeignException;

@Service
@RequiredArgsConstructor
public class EmployerReqImpl implements EmployerReq {

    private final RegistrationClient registrationClient;
    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public boolean leaveRequest(LeaveRequestDto leaveRequestDto) {
        try {
            // 1️⃣ Get user info from registration microservice
            UserDto user = registrationClient.getUserById(leaveRequestDto.getUserId());

            if (user == null) {
                System.out.println("User not found!");
                return false;
            }

            // 2️⃣ Map LeaveRequestDto → LeaveRequest entity
            LeaveRequest leaveRequest = new LeaveRequest();
            leaveRequest.setUserId(user.getId());
            leaveRequest.setFromDate(leaveRequestDto.getFromDate());
            leaveRequest.setToDate(leaveRequestDto.getToDate());
            leaveRequest.setFromTime(leaveRequestDto.getFromTime());
            leaveRequest.setToTime(leaveRequestDto.getToTime());
            leaveRequest.setDays(leaveRequestDto.getDays());
            leaveRequest.setState(RequestState.Pending);
            leaveRequest.setNote(leaveRequestDto.getNote());
            leaveRequest.setType(leaveRequestDto.getType());

            // 3️⃣ Save to DB
            leaveRequestRepository.save(leaveRequest);

            System.out.println("Leave request created for: " + user.getName());
            return true;

        } catch (FeignException.NotFound e) {
            // User with given ID does not exist
            System.err.println("User not found with ID: " + leaveRequestDto.getUserId());
            return false;

        } catch (FeignException e) {
            // Other Feign errors (service down, timeout, etc.)
            System.err.println("Error communicating with auth-service: " + e.getMessage());
            return false;

        } catch (Exception e) {
            // Any other unexpected errors
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public String cancelLeaveRequest(Long leaveRequestId, String observation) {
        try {
            // 1️⃣ Get the leave request from the DB
            LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
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

}