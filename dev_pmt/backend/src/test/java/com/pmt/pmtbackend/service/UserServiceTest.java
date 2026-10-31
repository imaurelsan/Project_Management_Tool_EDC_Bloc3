package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.LoginRequest;
import com.pmt.pmtbackend.dto.RegisterRequest;
import com.pmt.pmtbackend.exception.DuplicateResourceException;
import com.pmt.pmtbackend.exception.UnauthorizedException;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** Vérifie l'inscription et la connexion des utilisateurs. */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    @Test
    // Vérifie la normalisation de l'e-mail et le hachage du mot de passe.
    void registerNormaliseEmailEtEncodeLeMotDePasse() {
        when(userRepository.existsByEmail("member@test.local")).thenReturn(false);
        when(userRepository.existsByUsername("member")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");

        userService.register(new RegisterRequest("member", " MEMBER@TEST.LOCAL ", "secret"));

        verify(userRepository).save(argThat(user -> "member@test.local".equals(user.getEmail())
                && "encoded".equals(user.getPassword())));
    }

    @Test
    // Vérifie le refus d'une adresse e-mail déjà utilisée.
    void registerRefuseEmailExistant() {
        when(userRepository.existsByEmail("member@test.local")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.register(new RegisterRequest("member", "member@test.local", "secret")));
        verify(userRepository, never()).save(any());
    }

    @Test
    // Vérifie le refus d'un nom d'utilisateur déjà utilisé.
    void registerRefuseNomExistant() {
        when(userRepository.existsByEmail("member@test.local")).thenReturn(false);
        when(userRepository.existsByUsername("member")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.register(new RegisterRequest("member", "member@test.local", "secret")));
    }

    @Test
    // Vérifie une connexion avec un mot de passe correct.
    void loginAccepteMotDePasseCorrect() {
        User user = new User("member", "member@test.local", "encoded");
        when(userRepository.findByEmail("member@test.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        userService.login(new LoginRequest(" MEMBER@TEST.LOCAL ", "secret"));

        verify(passwordEncoder).matches("secret", "encoded");
    }

    @Test
    // Vérifie le refus d'identifiants inconnus ou incorrects.
    void loginRefuseUtilisateurInconnuOuMotDePasseIncorrect() {
        when(userRepository.findByEmail("unknown@test.local")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class,
                () -> userService.login(new LoginRequest("unknown@test.local", "secret")));

        User user = new User("member", "member@test.local", "encoded");
        when(userRepository.findByEmail("member@test.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);
        assertThrows(UnauthorizedException.class,
                () -> userService.login(new LoginRequest("member@test.local", "bad")));
    }
}
