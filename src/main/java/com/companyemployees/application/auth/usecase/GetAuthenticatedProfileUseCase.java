package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.user.UserId;
import org.springframework.stereotype.Service;

@Service
public class GetAuthenticatedProfileUseCase {

    private final UserRepository userRepository;

    public GetAuthenticatedProfileUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResult.AuthenticatedUser execute(String userId) {
        return userRepository.findById(new UserId(userId))
                .map(AuthResult.AuthenticatedUser::from)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));
    }
}
