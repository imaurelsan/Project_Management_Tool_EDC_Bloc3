package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.ProjectMember;
import com.pmt.pmtbackend.model.entity.ProjectRole;
import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité ProjectMember.
 * Permet de gérer l'appartenance des utilisateurs aux projets.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /**
     * Recherche l'association entre un projet et un utilisateur.
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

    /**
     * Récupère tous les membres d'un utilisateur.
     * @param user l'utilisateur
     * @return liste des membres de cet utilisateur
     */
    List<ProjectMember> findByUser(User user);

    /**
     * Récupère tous les membres d'un projet avec un rôle spécifique.
     * @param project le projet
     * @param role le rôle à filtrer
     * @return liste des membres du projet avec ce rôle
     */
    List<ProjectMember> findByProjectAndRole(Project project, ProjectRole role);
}