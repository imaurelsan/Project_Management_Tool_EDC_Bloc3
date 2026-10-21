package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.TaskDto;
import com.pmt.pmtbackend.dto.TaskRequest;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import com.pmt.pmtbackend.repository.ProjectRepository;
import com.pmt.pmtbackend.repository.TaskRepository;
import com.pmt.pmtbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des tâches d'un projet.
 * Les droits sont déduits du rôle de l'utilisateur dans le projet :
 * lecture pour tous les membres, écriture pour MEMBER et ADMINISTRATOR,
 * suppression et assignation réservées aux ADMINISTRATOR.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Liste les tâches d'un projet. Accessible à tout membre.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> getTasks(Long userId, Long projectId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        return taskRepository.findByProject(project).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Consulte une tâche. Accessible à tout membre du projet.
     */
    @Transactional(readOnly = true)
    public TaskDto getTask(Long userId, Long projectId, Long taskId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        return convertToDto(getTaskInProject(taskId, project));
    }

    /**
     * Crée une tâche. Réservé aux MEMBER et ADMINISTRATOR.
     * L'assignation n'est possible que pour les administrateurs.
     */
    @Transactional
    public TaskDto create(Long userId, Long projectId, TaskRequest request) {
        Project project = getProjectById(projectId);
        ProjectMember actor = getMembershipOrThrow(userId, project);
        requireCanWrite(actor);

        Task task = new Task();
        applyRequest(task, request, project, actor);
        taskRepository.save(task);
        return convertToDto(task);
    }

    /**
     * Modifie une tâche. Réservé aux MEMBER et ADMINISTRATOR.
     * Le changement d'assignation est réservé aux ADMINISTRATOR.
     */
    @Transactional
    public TaskDto update(Long userId, Long projectId, Long taskId, TaskRequest request) {
        Project project = getProjectById(projectId);
        ProjectMember actor = getMembershipOrThrow(userId, project);
        requireCanWrite(actor);

        Task task = getTaskInProject(taskId, project);
        applyRequest(task, request, project, actor);
        taskRepository.save(task);
        return convertToDto(task);
    }

    /**
     * Supprime une tâche. Réservé aux ADMINISTRATOR.
     */
    @Transactional
    public void delete(Long userId, Long projectId, Long taskId) {
        Project project = getProjectById(projectId);
        ProjectMember actor = getMembershipOrThrow(userId, project);
        if (actor.getRole() != ProjectRole.ADMINISTRATOR) {
            throw new ForbiddenException("Seuls les administrateurs peuvent supprimer une tâche.");
        }
        Task task = getTaskInProject(taskId, project);
        taskRepository.delete(task);
    }

    /**
     * Applique les champs d'une requête à une tâche, en vérifiant l'assignation.
     */
    private void applyRequest(Task task, TaskRequest request, Project project, ProjectMember actor) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(project);

        if (request.assigneeId() == null) {
            // Retirer l'assignation : autorisé pour un administrateur uniquement.
            if (task.getAssignee() != null && actor.getRole() != ProjectRole.ADMINISTRATOR) {
                throw new ForbiddenException("Seuls les administrateurs peuvent modifier l'assignation.");
            }
            task.setAssignee(null);
            return;
        }

        boolean assigneeChanged = task.getAssignee() == null
                || !task.getAssignee().getId().equals(request.assigneeId());
        if (assigneeChanged && actor.getRole() != ProjectRole.ADMINISTRATOR) {
            throw new ForbiddenException("Seuls les administrateurs peuvent modifier l'assignation.");
        }

        User assignee = userRepository.findById(request.assigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur assigné introuvable."));
        ProjectMember assigneeMembership = projectMemberRepository.findByProjectAndUser(project, assignee)
                .orElseThrow(() -> new ForbiddenException("L'utilisateur assigné n'est pas membre du projet."));

        if (assigneeMembership.getRole() == ProjectRole.VIEWER) {
            throw new ForbiddenException("Une tâche ne peut pas être assignée à un observateur (VIEWER).");
        }
        task.setAssignee(assignee);
    }

    private void requireCanWrite(ProjectMember actor) {
        if (actor.getRole() == ProjectRole.VIEWER) {
            throw new ForbiddenException("Les observateurs ne peuvent pas modifier les tâches.");
        }
    }

    private ProjectMember getMembershipOrThrow(Long userId, Project project) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
        return projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes pas membre de ce projet."));
    }

    private Task getTaskInProject(Long taskId, Project project) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty() || !task.get().getProject().getId().equals(project.getId())) {
            throw new ResourceNotFoundException("Tâche non trouvée dans ce projet.");
        }
        return task.get();
    }

    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé."));
    }

    private TaskDto convertToDto(Task task) {
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getAssignee() != null ? task.getAssignee().getUsername() : null,
                task.getCreatedAt() != null ? task.getCreatedAt().toString() : null,
                task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
    }
}