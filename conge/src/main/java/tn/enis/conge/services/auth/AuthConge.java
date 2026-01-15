package tn.enis.conge.services.auth;

import tn.enis.conge.dto.SignupRequest;
import tn.enis.conge.dto.UserDto;

public interface AuthConge {

    UserDto createEmployer(SignupRequest signupRequest);

    boolean hasCustomerWithEmail(String email);
}
