package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.AddMemberRequest;
import com.pmt.pmtbackend.dto.ProjectDto;
import com.pmt.pmtbackend.dto.ProjectMemberDto;
import com.pmt.pmtbackend.dto.ProjectRequest;
import com.pmt.pmtbackend.dto.UpdateMemberRoleRequest;
import com.pmt.pmtbackend.exception.DuplicateResourceException;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.ProjectMember;
import com.pmt.pmtbackend.model.entity.ProjectRole;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import com.pmt.pmtbackend.repository.ProjectRepository;
import com.pmt.pmtbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service de gestion des projets et de leurs membres.
 * Les règles d'accès sont vérifiées ici : un ADMINISTRATOR gère les membres,
 * un membre peut consulter le projet, et le dernier administrateur ne peut pas être rétrogradé.
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crée un projet et enregistre le créateur comme ADMINISTRATOR.
     *
     * @param request les informations du projet
     * @param actingUserId l'identifiant du créateur
     * @return le projet créé
     */
    @Transactional
    public ProjectDto createProject(ProjectRequest request, Long actingUserId) {
        User creator = getUserById(actingUserId);
        Project project = projectRepository.save(
                new Project(request.name(), request.description(), creator));

        projectMemberRepository.save(new ProjectMember(project, creator, ProjectRole.ADMINISTRATOR));

        return convertToDto(project);
    }

    /**
     * Liste les projets dont l'utilisateur est membre.
     *
     * @param actingUserId l'identifiant de l'utilisateur
     * @return la liste des projets
     */
    public List<ProjectDto> getProjectsForUser(Long actingUserId) {
        User user = getUserById(actingUserId);
        return projectMemberRepository.findByUser(user).stream()
                .map(ProjectMember::getProject)
                .distinct()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retourne un projet si l'utilisateur en est membre.
     *
     * @param projectId l'identifiant du projet
     * @param actingUserId l'identifiant de l'utilisateur
     * @return le projet
     * @throws ForbiddenException si l'utilisateur n'est pas membre du projet
     */
    public ProjectDto getProject(Long projectId, Long actingUserId) {
        User user = getUserById(actingUserId);
        Project project = getProjectById(projectId);
        requireMember(user, project);
        return convertToDto(project);
    }

    /**
     * Ajoute un utilisateur au projet avec le rôle demandé. Réservé aux administrateurs.
     *
     * @param projectId l'identifiant du projet
     * @param request l'email et le rôle du nouveau membre
     * @param actingUserId l'identifiant de l'utilisateur qui agit
     * @return le membre ajouté
     * @throws ForbiddenException si l'auteur n'est pas administrateur
     * @throws ResourceNotFoundException si aucun utilisateur ne correspond à cet email
     * @throws DuplicateResourceException si l'utilisateur est déjà membre
     */
    @Transactional
    public ProjectMemberDto addMember(Long projectId, AddMemberRequest request, Long actingUserId) {
        User actingUser = getUserById(actingUserId);
        Project project = getProjectById(projectId);
        requireAdministrator(actingUser, project);

        User newUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun utilisateur trouvé avec cet email."));

        if (findMembership(newUser, project).isPresent()) {
            throw new DuplicateResourceException("L'utilisateur est déjà membre de ce projet.");
        }

        ProjectMember member = projectMemberRepository.save(
                new ProjectMember(project, newUser, request.role()));
        return convertToDto(member);
    }

    /**
     * Modifie le rôle d'un membre. Réservé aux administrateurs.
     * Le dernier administrateur d'un projet ne peut pas être rétrogradé.
     *
     * @param projectId l'identifiant du projet
     * @param userId l'identifiant du membre concerné
     * @param request le nouveau rôle
     * @param actingUserId l'identifiant de l'utilisateur qui agit
     * @return le membre mis à jour
     * @throws ForbiddenException si l'auteur n'est pas administrateur, ou si l'on rétrograde le dernier administrateur
     * @throws ResourceNotFoundException si le membre n'existe pas dans ce projet
     */
    @Transactional
    public ProjectMemberDto updateMemberRole(Long projectId, Long userId,
                                             UpdateMemberRoleRequest request, Long actingUserId) {
        User actingUser = getUserById(actingUserId);
        Project project = getProjectById(projectId);
        requireAdministrator(actingUser, project);

        ProjectMember member = findMembership(getUserById(userId), project)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé dans ce projet."));

        boolean isDemotion = member.getRole() == ProjectRole.ADMINISTRATOR
                && request.role() != ProjectRole.ADMINISTRATOR;
        if (isDemotion) {
            long adminCount = projectMemberRepository.findByProjectAndRole(project, ProjectRole.ADMINISTRATOR).size();
            if (adminCount <= 1) {
                throw new ForbiddenException("Impossible de retirer le rôle d'administrateur au dernier administrateur.");
            }
        }

        member.setRole(request.role());
        return convertToDto(projectMemberRepository.save(member));
    }

    /**
     * Liste les membres d'un projet. Accessible à tous les membres.
     *
     * @param projectId l'identifiant du projet
     * @param actingUserId l'identifiant de l'utilisateur qui agit
     * @return la liste des membres
     * @throws ForbiddenException si l'utilisateur n'est pas membre du projet
     */
    public List<ProjectMemberDto> getMembers(Long projectId, Long actingUserId) {
        User user = getUserById(actingUserId);
        Project project = getProjectById(projectId);
        requireMember(user, project);

        return projectMemberRepository.findByProject(project).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ---------- Méthodes privées ----------

    private Optional<ProjectMember> findMembership(User user, Project project) {
        return projectMemberRepository.findByProjectAndUser(project, user);
    }

    private void requireMember(User user, Project project) {
        if (findMembership(user, project).isEmpty()) {
            throw new ForbiddenException("L'utilisateur n'est pas membre de ce projet.");
        }
    }

    private void requireAdministrator(User user, Project project) {
        boolean isAdmin = findMembership(user, project)
                .map(pm -> pm.getRole() == ProjectRole.ADMINISTRATOR)
                .orElse(false);
        if (!isAdmin) {
            throw new ForbiddenException("Seuls les administrateurs peuvent effectuer cette action.");
        }
    }

    private ProjectDto convertToDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt() != null ? project.getCreatedAt().toString() : null,
                project.getOwner().getId(),
                project.getOwner().getUsername());
    }

    private ProjectMemberDto convertToDto(ProjectMember member) {
        return new ProjectMemberDto(
                member.getId(),
                member.getUser().getUsername(),
                member.getUser().getEmail(),
                member.getRole());
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé."));
    }
}