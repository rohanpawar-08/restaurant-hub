package com.restauranthub.auth;

import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import java.util.List;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService adapter.
 * Loads User entity from MySQL by email and translates into Spring Security UserDetails.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new DisabledException("User account is inactive");
        }

        String roleAuthority = "ROLE_" + user.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Boolean.TRUE.equals(user.getActive()),
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority(roleAuthority))
        );
    }
}
