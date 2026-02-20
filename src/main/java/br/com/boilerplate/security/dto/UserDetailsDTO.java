package br.com.boilerplate.security.dto;

import br.com.boilerplate.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class UserDetailsDTO implements UserDetails {

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    public Map<String, ?> toMap(){
        return Map.of(
                "id", this.user.getId().toString(),
                "name", this.user.getName(),
                "email", this.user.getEmail(),
                "role", this.user.getRole().name()
        );
    }
}
