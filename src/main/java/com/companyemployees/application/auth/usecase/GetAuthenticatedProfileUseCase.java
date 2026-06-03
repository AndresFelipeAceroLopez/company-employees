package com.companyemployees.application.auth.usecase;

import com.companyemployees.application.auth.UserAuthorizationResolver;
import com.companyemployees.application.auth.dto.AuthResult;
import com.companyemployees.application.ports.repository.UserRepository;
import com.companyemployees.domain.common.EntityNotFoundException;
import com.companyemployees.domain.user.User;
import com.companyemployees.domain.user.UserId;
import org.springframework.stereotype.Service;

@Service
public class GetAuthenticatedProfileUseCase {

    private final UserRepository userRepository;
    private final UserAuthorizationResolver authorizationResolver;

    public GetAuthenticatedProfileUseCase(UserRepository userRepository,
                                          UserAuthorizationResolver authorizationResolver) {
        this.userRepository = userRepository;
        this.authorizationResolver = authorizationResolver;
    }

    public AuthResult.AuthenticatedUser execute(String userId) {
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + userId));
        UserAuthorizationResolver.ResolvedAuthorization authz = authorizationResolver.resolve(user);
        return AuthResult.AuthenticatedUser.of(user, authz.roleNames(), authz.scopes());
    }
}
