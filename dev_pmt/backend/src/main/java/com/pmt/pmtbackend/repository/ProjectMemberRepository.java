package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.ProjectMember;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour l'entité ProjectMember.
 * Permet de gérer l'appartenance des utilisateurs aux projets.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Recherche l'association entre un projet et un utilisateur.
     * Permet de récupérer le rôle d'un utilisateur dans un projet donné.
     * @param project le projet
     * @param user l'utilisateur
     * @return Optional contenant l'association trouvée
     */
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    /**
     * Récupère tous les membres d'un projet donné.
     * @param project le projet
     * @return liste des membres du projet
     */
    List<ProjectMember> findByProject(Project project);
}