package tn.enis.DemandeConge.services.EmployeReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.enis.DemandeConge.client.RegistrationClient;
import tn.enis.DemandeConge.dto.LeaveRequestDto;
import tn.enis.DemandeConge.dto.UserDto;
import tn.enis.DemandeConge.entity.LeaveRequest;
import tn.enis.DemandeConge.enums.RequestState;
import tn.enis.DemandeConge.repository.LeaveRequestRepository;
import tn.enis.DemandeConge.services.EmployeReq.EmployerReq;

@Service
@RequiredArgsConstructor
public class EmployerReqImpl implements EmployerReq {

    private final RegistrationClient registrationClient; // Feign client
    private final LeaveRequestRepository leaveRequestRepository;

    @Override
    public boolean leaveRequest(LeaveRequestDto leaveRequestDto) {
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
        leaveRequest.setState(RequestState.Pending); // default state
        leaveRequest.setNote(leaveRequestDto.getNote());
        leaveRequest.setType(leaveRequestDto.getType());

        // 3️⃣ Save to DB
        leaveRequestRepository.save(leaveRequest);

        System.out.println("Leave request created for: " + user.getName());
        return true;
    }
}
