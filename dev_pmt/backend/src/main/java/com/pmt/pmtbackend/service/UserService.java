
import com.pmt.pmtbackend.dto.LoginRequest;
import com.pmt.pmtbackend.dto.RegisterRequest;
import com.pmt.pmtbackend.dto.UserDto;
import com.pmt.pmtbackend.exception.DuplicateResourceException;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs.
 * Permet d'enregistrer, de connecter et de récupérer les informations d'un utilisateur.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructeur avec injection des dépendances.
     * @param userRepository le repository pour accéder aux utilisateurs
     * @param passwordEncoder l'encodeur de mot de passe BCrypt
     */
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur.
     * Vérifie si l'e-mail existe déjà et lance une exception si c'est le cas.
     * Encode le mot de passe avant d'enregistrer l'utilisateur.
     * @param registerRequest les informations d'enregistrement
     * @return le DTO de l'utilisateur créé
     * @throws DuplicateResourceException si l'e-mail est déjà utilisé
     */
    @Transactional
    public UserDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new DuplicateResourceException("Cet e-mail est déjà utilisé.");
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));

        userRepository.save(user);
        return toUserDto(user);
    }

    /**
     * Connecte un utilisateur.
     * Vérifie si l'e-mail existe et compare le mot de passe encodé avec celui fourni.
     * @param loginRequest les informations de connexion
     * @return le DTO de l'utilisateur connecté
     * @throws ResourceNotFoundException si l'e-mail n'existe pas
     * @throws ForbiddenException si le mot de passe est incorrect
     */
    @Transactional(readOnly = true)
    public UserDto login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ForbiddenException("Identifiants invalides."));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new ForbiddenException("Identifiants invalides.");
        }

        return toUserDto(user);
    }
    /**
     * Récupère un utilisateur par son ID.
     * @param id l'ID de l'utilisateur à récupérer
     * @return le DTO de l'utilisateur trouvé
     * @throws ResourceNotFoundException si aucun utilisateur n'est trouvé avec cet ID
     */
    @Transactional(readOnly = true)
    public UserDto getById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("Aucun utilisateur trouvé avec cet ID.");
        }

        return toUserDto(userOptional.get());
    }

    /**
     * Convertit un objet User en UserDto.
     * @param user l'objet User à convertir
     * @return le DTO correspondant
     */
    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}