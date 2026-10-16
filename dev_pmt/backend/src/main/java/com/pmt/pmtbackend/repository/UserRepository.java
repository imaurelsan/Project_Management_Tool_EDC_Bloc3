package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository pour l'entité User.
 * Permet de gérer les opérations CRUD et les recherches d'utilisateurs.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     * @param username le nom d'utilisateur (unique)
     * @return Optional contenant l'utilisateur trouvé
     */
    Optional<User> findByUsername(String username);

    /**
     * Vérifie si un utilisateur existe déjà avec l'adresse e-mail spécifiée.
     * @param email l'e-mail à vérifier
     * @return true si un utilisateur existe avec cet e-mail, false sinon
     */
    boolean existsByEmail(String email);
    /**
     * Vérifie si un utilisateur existe déjà avec le nom d'utilisateur spécifié.
     * @param username le nom d'utilisateur à vérifier
     * @return true si un utilisateur existe avec ce nom, false sinon
     */
    boolean existsByUsername(String username);

    /**
     * Recherche un utilisateur par son adresse e-mail.
     * @param email l'adresse e-mail (unique)
     * @return Optional contenant l'utilisateur trouvé
     */
    Optional<User> findByEmail(String email);
}