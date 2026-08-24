package com.restauranthub.auth;

import com.restauranthub.auth.dto.LoginRequest;
import com.restauranthub.auth.dto.RegisterRequest;
import com.restauranthub.auth.dto.UserResponse;
import com.restauranthub.auth.exception.DuplicateEmailException;
import com.restauranthub.auth.exception.DuplicatePhoneException;
import com.restauranthub.user.User;
import com.restauranthub.user.UserRepository;
import com.restauranthub.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing customer authentication workflows:
 * registration, login session establishment, current user lookup, and logout.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Registers a new customer in the system.
     * Enforces unique email and phone constraints and hashes the raw password using BCrypt.
     * Always assigns the CUSTOMER role.
     *
     * @param request validated registration details
     * @return safe UserResponse without sensitive credential information
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedPhone = request.phone().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateEmailException("An account with this email address already exists.");
        }

        if (userRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicatePhoneException("An account with this mobile number already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User newUser = new User(
                request.fullName().trim(),
                normalizedEmail,
                normalizedPhone,
                encodedPassword,
                UserRole.CUSTOMER
        );

        User savedUser = userRepository.save(newUser);
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Authenticates a user against Spring Security and establishes a server-side HTTP session.
     *
     * @param request     login credentials
     * @param httpRequest current HTTP servlet request
     * @param httpResponse current HTTP servlet response
     * @return authenticated UserResponse
     */
    public UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String normalizedEmail = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password. Please try again."));

        return UserResponse.fromEntity(user);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param authentication active security authentication principal
     * @return current user details
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User is not authenticated.");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Authenticated user not found in database."));

        return UserResponse.fromEntity(user);
    }

    /**
     * Invalidates the server-side HTTP session and clears the SecurityContext.
     *
     * @param httpRequest current HTTP servlet request
     * @param httpResponse current HTTP servlet response
     */
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        securityContextRepository.saveContext(emptyContext, httpRequest, httpResponse);
    }
}
