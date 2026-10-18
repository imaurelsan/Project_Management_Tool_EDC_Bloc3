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

/**
 * Service de gestion des projets et de leurs membres.
 * Les règles d'accès sont déléguées à {@link ProjectAccessPolicy}.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectAccessPolicy accessPolicy;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectMemberRepository projectMemberRepository,
                          UserRepository userRepository,
                          ProjectAccessPolicy accessPolicy) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.accessPolicy = accessPolicy;
    }

    /**
     * Crée un projet. L'utilisateur courant en devient propriétaire et administrateur.
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
     */
    @Transactional(readOnly = true)
    public ProjectDto getById(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireMembership(user, project);
        return convertToDto(project);
    }

    /**
     * Met à jour un projet. Réservé aux administrateurs.
     */
    @Transactional
    public ProjectDto update(Long userId, Long projectId, ProjectRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireAdministrator(user, project);

        project.setName(request.name());
        project.setDescription(request.description());
        projectRepository.save(project);
        return convertToDto(project);
    }

    /**
     * Supprime un projet. Réservé aux administrateurs.
     */
    @Transactional
    public void delete(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireAdministrator(user, project);
        projectRepository.delete(project);
    }

    /**
     * Liste les membres d'un projet dont l'utilisateur fait partie.
     */
    @Transactional(readOnly = true)
    public List<ProjectMemberDto> getMembers(Long userId, Long projectId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireMembership(user, project);
        return projectMemberRepository.findByProject(project).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Ajoute un utilisateur existant à un projet. Réservé aux administrateurs.
     */
    @Transactional
    public ProjectMemberDto addMember(Long userId, Long projectId, AddMemberRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireAdministrator(user, project);

        String email = normalizeEmail(request.email());
        User newMember = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun utilisateur trouvé avec cet e-mail."));

        if (accessPolicy.findMembership(newMember, project).isPresent()) {
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
     * Modifie le rôle d'un membre. Réservé aux administrateurs.
     * Interdit de rétrograder le dernier administrateur du projet.
     */
    @Transactional
    public ProjectMemberDto updateMemberRole(Long userId, Long projectId, Long memberUserId,
                                             UpdateMemberRoleRequest request) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireAdministrator(user, project);

        User target = getUserById(memberUserId);
        ProjectMember membership = accessPolicy.findMembership(target, project)
                .orElseThrow(() -> new ResourceNotFoundException("Cet utilisateur n'est pas membre du projet."));

        boolean losingAdmin = membership.getRole() == ProjectRole.ADMINISTRATOR
                && request.role() != ProjectRole.ADMINISTRATOR;
        if (losingAdmin) {
            ensureNotLastAdministrator(project);
        }

        membership.setRole(request.role());
        projectMemberRepository.save(membership);
        return convertToDto(membership);
    }

    /**
     * Retire un membre d'un projet. Réservé aux administrateurs.
     * Interdit de retirer le dernier administrateur du projet.
     */
    @Transactional
    public void removeMember(Long userId, Long projectId, Long memberUserId) {
        User user = getUserById(userId);
        Project project = getProjectById(projectId);
        accessPolicy.requireAdministrator(user, project);

        User target = getUserById(memberUserId);
        ProjectMember membership = accessPolicy.findMembership(target, project)
                .orElseThrow(() -> new ResourceNotFoundException("Cet utilisateur n'est pas membre du projet."));

        if (membership.getRole() == ProjectRole.ADMINISTRATOR) {
            ensureNotLastAdministrator(project);
        }

        projectMemberRepository.delete(membership);
    }

    /**
     * Vérifie qu'il restera au moins un administrateur après l'opération.
     * @throws ForbiddenException si l'opération supprimerait le dernier administrateur
     */
    private void ensureNotLastAdministrator(Project project) {
        long adminCount = projectMemberRepository.findByProjectAndRole(project, ProjectRole.ADMINISTRATOR).size();
        if (adminCount <= 1) {
            throw new ForbiddenException("Un projet doit conserver au moins un administrateur.");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
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
                member.getUser().getId(),
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