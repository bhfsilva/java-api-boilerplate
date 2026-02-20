package br.com.boilerplate.services;

import br.com.boilerplate.entities.User;
import br.com.boilerplate.enums.UserRole;
import br.com.boilerplate.errors.exceptions.EntityNotFoundException;
import br.com.boilerplate.errors.exceptions.ForbiddenException;
import br.com.boilerplate.security.dto.UserDetailsDTO;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthenticationContextService {

    public UserDetailsDTO getData() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Optional)
            return this.handleOptionalData((Optional<UserDetailsDTO>) principal);

        if (principal instanceof UserDetailsDTO)
            return (UserDetailsDTO) principal;

        throw new ForbiddenException();
    }

    private UserDetailsDTO handleOptionalData(Optional<UserDetailsDTO> principal) {
        principal.orElseThrow(() -> new EntityNotFoundException(UserDetailsDTO.class));
        return principal.get();
    }

    public User getAuthenticatedUser() {
        return this.getData().getUser();
    }

    public Boolean isAuthenticatedUser(User user) {
        return this.getAuthenticatedUser().getId().equals(user.getId());
    }

    public Boolean isAuthenticatedUserAdmin() {
        return this.getAuthenticatedUser().getRole().equals(UserRole.ADMIN);
    }
}
