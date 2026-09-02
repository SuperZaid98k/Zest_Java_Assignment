package com.zest.productapi.service;

import com.zest.productapi.dto.request.LoginRequest;
import com.zest.productapi.dto.request.RegisterRequest;
import com.zest.productapi.dto.response.TokenResponse;
import com.zest.productapi.entity.RefreshToken;
import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.User;
import com.zest.productapi.exception.BadRequestException;
import com.zest.productapi.repository.RefreshTokenRepository;
import com.zest.productapi.repository.UserRepository;
import com.zest.productapi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AsyncAuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowBadRequestException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void register_WhenValid_ShouldReturnTokens() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .role("USER")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(tokenProvider.generateTokenFromUsername("newuser")).thenReturn("mockJwtToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        TokenResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockJwtToken", response.getAccessToken());
        assertEquals("newuser", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_WhenValid_ShouldReturnTokens() {
        LoginRequest request = LoginRequest.builder()
                .username("john")
                .password("secret")
                .build();

        User user = User.builder()
                .id(1L)
                .username("john")
                .password("encodedSecret")
                .role(Role.ROLE_USER)
                .build();

        Authentication auth = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(tokenProvider.generateTokenFromUsername("john")).thenReturn("mockAccessToken");

        TokenResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockAccessToken", response.getAccessToken());
        assertEquals("john", response.getUsername());
    }
}
