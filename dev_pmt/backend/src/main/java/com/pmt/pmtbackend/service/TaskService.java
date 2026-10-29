package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.HistoryDto;
import com.pmt.pmtbackend.dto.TaskDto;
import com.pmt.pmtbackend.dto.TaskRequest;
import com.pmt.pmtbackend.exception.BadRequestException;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.ProjectMember;
import com.pmt.pmtbackend.model.entity.ProjectRole;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.HistoryRepository;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import com.pmt.pmtbackend.repository.ProjectRepository;
import com.pmt.pmtbackend.repository.TaskRepository;
import com.pmt.pmtbackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des tâches d'un projet.
 * - lecture : tous les membres (ADMINISTRATOR, MEMBER, VIEWER)
 * - création, modification et assignation : ADMINISTRATOR et MEMBER
 * - suppression : ADMINISTRATOR uniquement.
 * - chaque modification est tracée dans l'historique.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final HistoryService historyService;

    public TaskService(TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            HistoryRepository historyRepository,
            HistoryService historyService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.historyService = historyService;
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getTasks(Long userId, Long projectId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        return taskRepository.findByProject(project).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(Long userId, Long projectId, Long taskId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        return convertToDto(getTaskInProject(taskId, project));
    }

    @Transactional
    public TaskDto create(Long userId, Long projectId, TaskRequest request) {
        Project project = getProjectById(projectId);
        ProjectMember actor = getMembershipOrThrow(userId, project);
        requireCanWrite(actor);

        // Validation de l'assignation avant toute modification de l'entité.
        User assignee = resolveAssignee(request.assigneeId(), project);

        Task task = new Task();
        applyFields(task, request, project, assignee);
        taskRepository.save(task);

        // Création : anciennes valeurs null, on trace chaque champ renseigné.
        User author = actor.getUser();
        historyService.record("creation", null, task.getTitle(), task, author);
        historyService.record("status", null, task.getStatus(), task, author);
        historyService.record("priority", null, task.getPriority(), task, author);
        historyService.record("assignee", null,
                assignee != null ? assignee.getUsername() : null, task, author);

        return convertToDto(task);
    }

    @Transactional
    public TaskDto update(Long userId, Long projectId, Long taskId, TaskRequest request) {
        Project project = getProjectById(projectId);
        ProjectMember actor = getMembershipOrThrow(userId, project);
        requireCanWrite(actor);

        Task task = getTaskInProject(taskId, project);

        // Validation de l'assignation avant toute modification de l'entité.
        User assignee = resolveAssignee(request.assigneeId(), project);

        // Mémorisation des anciennes valeurs avant modification.
        User author = actor.getUser();
        String oldTitle = task.getTitle();
        String oldDescription = task.getDescription();
        Object oldStatus = task.getStatus();
        Object oldPriority = task.getPriority();
        Object oldDueDate = task.getDueDate();
        User oldAssignee = task.getAssignee();

        applyFields(task, request, project, assignee);
        taskRepository.save(task);

        historyService.record("title", oldTitle, task.getTitle(), task, author);
        historyService.record("description", oldDescription, task.getDescription(), task, author);
        historyService.record("status", oldStatus, task.getStatus(), task, author);
        historyService.record("priority", oldPriority, task.getPriority(), task, author);
        historyService.record("dueDate", oldDueDate, task.getDueDate(), task, author);
        historyService.record("assignee",
                oldAssignee != null ? oldAssignee.getUsername() : null,
                assignee != null ? assignee.getUsername() : null, task, author);

        return convertToDto(task);
    }

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
     * Retourne l'historique d'une tâche, du plus récent au plus ancien. Accessible à tous les membres.
     */
    @Transactional(readOnly = true)
    public List<HistoryDto> getHistory(Long userId, Long projectId, Long taskId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        Task task = getTaskInProject(taskId, project);
        return historyRepository.findByTaskOrderByModifiedDateDesc(task).stream()
                .map(h -> new HistoryDto(h.getId(), h.getModifiedField(), h.getOldValue(),
                        h.getNewValue(), h.getModifiedDate() != null ? h.getModifiedDate().toString() : null,
                        h.getModifiedBy().getUsername()))
                .toList();
    }

    /**
     * Vérifie et résout l'assigné demandé, sans modifier la tâche.
     * L'assignation est permise aux ADMINISTRATOR et MEMBER (l'appelant a déjà
     * été validé par requireCanWrite). Retourne null si l'assignation est retirée.
     */
    private User resolveAssignee(Long assigneeId, Project project) {
        if (assigneeId == null) {
            return null;
        }
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur assigné introuvable."));

        ProjectMember assigneeMembership = projectMemberRepository.findByProjectAndUser(project, assignee)
                .orElseThrow(() -> new BadRequestException("L'utilisateur assigné n'est pas membre du projet."));

        if (assigneeMembership.getRole() == ProjectRole.VIEWER) {
            throw new BadRequestException("Une tâche ne peut pas être assignée à un observateur (VIEWER).");
        }
        return assignee;
    }

    /**
     * Applique les champs simples et l'assigné déjà validé.
     */
    private void applyFields(Task task, TaskRequest request, Project project, User assignee) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(project);
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
        return taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée dans ce projet."));
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