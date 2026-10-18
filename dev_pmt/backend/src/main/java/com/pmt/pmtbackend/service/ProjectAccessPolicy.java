package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.ProjectMember;
import com.pmt.pmtbackend.model.entity.ProjectRole;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Centralise les règles d'accès aux projets selon le rôle de l'utilisateur.
 * Tous les services passent par cette classe : une seule source de vérité.
 */
@Component
public class ProjectAccessPolicy {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectAccessPolicy(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    /**
     * Retourne l'appartenance de l'utilisateur au projet, si elle existe.
     */
    public Optional<ProjectMember> findMembership(User user, Project project) {
        return projectMemberRepository.findByProjectAndUser(project, user);
    }

    /**
     * Vérifie que l'utilisateur est membre du projet, quel que soit son rôle.
     * @throws ForbiddenException si ce n'est pas le cas
     */
    public void requireMembership(User user, Project project) {
        if (findMembership(user, project).isEmpty()) {
            throw new ForbiddenException("Vous n'êtes pas membre de ce projet.");
        }
    }

    /**
     * Vérifie que l'utilisateur peut modifier le contenu du projet (ADMINISTRATOR ou MEMBER).
     * @throws ForbiddenException si l'utilisateur est observateur ou non membre
     */
    public void requireWriteAccess(User user, Project project) {
        ProjectRole role = roleOf(user, project);
        if (role != ProjectRole.ADMINISTRATOR && role != ProjectRole.MEMBER) {
            throw new ForbiddenException("Votre rôle ne permet pas de modifier ce projet.");
        }
    }

    /**
     * Vérifie que l'utilisateur est administrateur du projet.
     * @throws ForbiddenException si ce n'est pas le cas
     */
    public void requireAdministrator(User user, Project project) {
        if (roleOf(user, project) != ProjectRole.ADMINISTRATOR) {
            throw new ForbiddenException("Seuls les administrateurs peuvent effectuer cette action.");
        }
    }

    /**
     * Retourne le rôle de l'utilisateur dans le projet.
     * @throws ForbiddenException si l'utilisateur n'est pas membre
     */
    public ProjectRole roleOf(User user, Project project) {
        return findMembership(user, project)
                .map(ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes pas membre de ce projet."));
    }
}