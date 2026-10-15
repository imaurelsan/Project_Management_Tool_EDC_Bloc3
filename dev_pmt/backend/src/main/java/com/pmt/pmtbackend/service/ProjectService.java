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

/**
 * Service de gestion des projets et de leurs membres.
 * Les actions d'administration sont réservées aux membres ayant le rôle ADMINISTRATOR.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    /**
     * Constructeur avec injection des dépendances.
     *
     * @param projectRepository le repository des projets
     * @param projectMemberRepository le repository des appartenances projet/utilisateur
     * @param userRepository le repository des utilisateurs
     */
    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crée un projet. L'utilisateur courant en devient propriétaire et administrateur.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param request les données du projet
     * @return le projet créé
     */
    @Transactional
    public ProjectDto create(Long userId, ProjectRequest request) {
        User owner = getUserById(userId);

        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwner(owner);
        projectRepository.save(project);

        ProjectMember membership = new ProjectMember();
        membership.setProject(project);
        membership.setUser(owner);
        membership.setRole(ProjectRole.ADMINISTRATOR);
        projectMemberRepository.save(membership);

        return convertToDto(project);
    }

    /**
     * Liste les projets dont l'utilisateur est membre.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @return la liste des projets accessibles
     */
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllForUser(Long userId) {
        User user = getUserById(userId);
        return projectMemberRepository.findByUser(user).stream()
                .map(ProjectMember::getProject)
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Récupère un projet dont l'utilisateur est membre.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @return le projet demandé
     */
    @Transactional(readOnly = true)
    public ProjectDto getById(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireMembership(user, project);
        return convertToDto(project);
    }

    /**
     * Met à jour un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param request les nouvelles données
     * @return le projet mis à jour
     */
    @Transactional
    public ProjectDto update(Long userId, Long projectId, ProjectRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireAdministrator(user, project);

        project.setName(request.name());
        project.setDescription(request.description());
        projectRepository.save(project);
        return convertToDto(project);
    }

    /**
     * Supprime un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     */
    @Transactional
    public void delete(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireAdministrator(user, project);
        projectRepository.delete(project);
    }

    /**
     * Liste les membres d'un projet dont l'utilisateur fait partie.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @return la liste des membres
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberDto> getMembers(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireMembership(user, project);
        return projectMemberRepository.findByProject(project).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Ajoute un utilisateur existant à un projet. Réservé aux administrateurs.
     * L'e-mail est normalisé avant la recherche.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param request l'e-mail du membre et son rôle
     * @return le membre ajouté
     */
    @Transactional
    public ProjectMemberDto addMember(Long userId, Long projectId, AddMemberRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireAdministrator(user, project);

        String email = normalizeEmail(request.email());
        User newMember = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun utilisateur trouvé avec cet e-mail."));

        if (findMembership(newMember, project).isPresent()) {
            throw new DuplicateResourceException("Cet utilisateur est déjà membre du projet.");
        }

        ProjectMember membership = new ProjectMember();
        membership.setProject(project);
        membership.setUser(newMember);
        membership.setRole(request.role());
        projectMemberRepository.save(membership);
        return convertToDto(membership);
    }

    /**
     * Modifie le rôle d'un membre. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param memberUserId l'identifiant de l'utilisateur membre à modifier
     * @param request le nouveau rôle
     * @return le membre mis à jour
     */
    @Transactional
    public ProjectMemberDto updateMemberRole(Long userId, Long projectId, Long memberUserId,
                                             UpdateMemberRoleRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireAdministrator(user, project);

        User target = getUserById(memberUserId);
        ProjectMember membership = findMembership(target, project)
                .orElseThrow(() -> new ResourceNotFoundException("Cet utilisateur n'est pas membre du projet."));

        membership.setRole(request.role());
        projectMemberRepository.save(membership);
        return convertToDto(membership);
    }

    /**
     * Retire un membre d'un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param memberUserId l'identifiant de l'utilisateur à retirer
     */
    @Transactional
    public void removeMember(Long userId, Long projectId, Long memberUserId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        requireAdministrator(user, project);

        User target = getUserById(memberUserId);
        ProjectMember membership = findMembership(target, project)
                .orElseThrow(() -> new ResourceNotFoundException("Cet utilisateur n'est pas membre du projet."));

        projectMemberRepository.delete(membership);
    }

    /**
     * Recherche l'appartenance d'un utilisateur à un projet.
     *
     * @param user l'utilisateur
     * @param project le projet
     * @return l'appartenance si elle existe
     */
    private Optional<ProjectMember> findMembership(User user, Project project) {
        return projectMemberRepository.findByProjectAndUser(project, user);
    }

    /**
     * Vérifie que l'utilisateur est membre du projet.
     *
     * @param user l'utilisateur
     * @param project le projet
     * @throws ForbiddenException si l'utilisateur n'est pas membre
     */
    private void requireMembership(User user, Project project) {
        if (findMembership(user, project).isEmpty()) {
            throw new ForbiddenException("Vous n'êtes pas membre de ce projet.");
        }
    }

    /**
     * Vérifie que l'utilisateur est administrateur du projet.
     *
     * @param user l'utilisateur
     * @param project le projet
     * @throws ForbiddenException si l'utilisateur n'est pas administrateur
     */
    private void requireAdministrator(User user, Project project) {
        boolean isAdmin = findMembership(user, project)
                .map(pm -> pm.getRole() == ProjectRole.ADMINISTRATOR)
                .orElse(false);
        if (!isAdmin) {
            throw new ForbiddenException("Seuls les administrateurs peuvent effectuer cette action.");
        }
    }

    /**
     * Normalise un e-mail : suppression des espaces et passage en minuscules.
     *
     * @param email l'e-mail brut
     * @return l'e-mail normalisé
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    /**
     * Convertit un projet en DTO.
     *
     * @param project le projet
     * @return le DTO correspondant
     */
    private ProjectDto convertToDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt() != null ? project.getCreatedAt().toString() : null,
                project.getOwner().getId(),
                project.getOwner().getUsername());
    }

    /**
     * Convertit une appartenance en DTO.
     *
     * @param member l'appartenance
     * @return le DTO correspondant
     */
    private ProjectMemberDto convertToDto(ProjectMember member) {
        return new ProjectMemberDto(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getUser().getEmail(),
                member.getRole());
    }

    /**
     * Charge un utilisateur par son identifiant.
     *
     * @param userId l'identifiant
     * @return l'utilisateur
     * @throws ResourceNotFoundException si introuvable
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    /**
     * Charge un projet par son identifiant.
     *
     * @param projectId l'identifiant
     * @return le projet
     * @throws ResourceNotFoundException si introuvable
     */
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé."));
    }
}