package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.LoginRequest;
import com.pmt.pmtbackend.dto.RegisterRequest;
import com.pmt.pmtbackend.dto.UserDto;
import com.pmt.pmtbackend.exception.DuplicateResourceException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.exception.UnauthorizedException;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service pour la gestion des utilisateurs.
 * Permet d'enregistrer, de connecter et de récupérer les informations d'un
 * utilisateur.
 * Les e-mails sont normalisés (espaces retirés, minuscules) avant tout
 * traitement.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param userRepository  le repository pour accéder aux utilisateurs
     * @param passwordEncoder l'encodeur de mot de passe BCrypt
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur.
     * Vérifie que l'e-mail n'est pas déjà utilisé, puis encode le mot de passe.
     *
     * @param registerRequest les informations d'enregistrement
     * @return le DTO de l'utilisateur créé
     * @throws DuplicateResourceException si l'e-mail est déjà utilisé
     */
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        String email = normalizeEmail(registerRequest.email());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Cet e-mail est déjà utilisé.");
        }
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur est déjà pris.");
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));

        userRepository.save(user);
        return toUserDto(user);
    }

    /**
     * Connecte un utilisateur à partir de son e-mail et de son mot de passe.
     * Le message d'erreur est identique que l'e-mail soit inconnu ou que le mot de
     * passe soit faux,
     * afin de ne pas révéler l'existence d'un compte.
     *
     * @param loginRequest les informations de connexion
     * @return le DTO de l'utilisateur connecté
     * @throws UnauthorizedException si l'e-mail est inconnu ou le mot de passe
     *                               incorrect
     */
    @Transactional(readOnly = true)
    public UserDto login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(normalizeEmail(loginRequest.email()))
                .orElseThrow(() -> new UnauthorizedException("Identifiants invalides."));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new UnauthorizedException("Identifiants invalides.");
        }

        return toUserDto(user);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     *
     * @param id l'identifiant de l'utilisateur
     * @return le DTO de l'utilisateur trouvé
     * @throws ResourceNotFoundException si aucun utilisateur ne correspond à cet
     *                                   identifiant
     */
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun utilisateur trouvé avec cet ID."));
        return toUserDto(user);
    }

    /**
     * Normalise une adresse e-mail : suppression des espaces en début et fin, puis
     * passage en minuscules.
     *
     * @param email l'e-mail brut saisi par l'utilisateur
     * @return l'e-mail normalisé
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    /**
     * Convertit un objet User en UserDto. Le mot de passe n'est jamais exposé.
     *
     * @param user l'objet User à convertir
     * @return le DTO correspondant
     */
    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}