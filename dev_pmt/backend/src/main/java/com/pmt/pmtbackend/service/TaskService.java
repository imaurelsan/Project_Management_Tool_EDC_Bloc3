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
import java.util.Objects;

/**
 * Service de gestion des tâches d'un projet.
 * - lecture : tous les membres (ADMINISTRATOR, MEMBER, VIEWER)
 * - création, modification et assignation : ADMINISTRATOR et MEMBER
 * - suppression : ADMINISTRATOR uniquement.
 * - chaque modification est tracée dans l'historique.
 * - chaque nouvelle assignation déclenche une notification (base + e-mail).
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;
    private final HistoryService historyService;
    private final NotificationService notificationService;

    /**
     * Construit le service de gestion des tâches.
     *
     * @param taskRepository          dépôt des tâches
     * @param projectRepository       dépôt des projets
     * @param projectMemberRepository dépôt des membres des projets
     * @param userRepository          dépôt des utilisateurs
     * @param historyRepository       dépôt de l'historique
     * @param historyService          service d'enregistrement de l'historique
     * @param notificationService     service de notification d'assignation
     */
    public TaskService(TaskRepository taskRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            HistoryRepository historyRepository,
            HistoryService historyService,
            NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.historyService = historyService;
        this.notificationService = notificationService;
    }

    /**
     * Liste les tâches d'un projet. Accessible à tous les membres.
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
     * Retourne le détail d'une tâche. Accessible à tous les membres.
     */
    @Transactional(readOnly = true)
    public TaskDto getTask(Long userId, Long projectId, Long taskId) {
        Project project = getProjectById(projectId);
        getMembershipOrThrow(userId, project);
        return convertToDto(getTaskInProject(taskId, project));
    }

    /**
     * Crée une tâche (ADMINISTRATOR et MEMBER). Trace la création dans l'historique
     * et notifie l'assigné s'il y en a un.
     *
     * @param userId    identifiant de l'auteur
     * @param projectId identifiant du projet
     * @param request   données de la tâche à créer
     * @return tâche créée sous forme de DTO
     */
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

        // Notification : à la création, tout assigné est un "nouvel" assigné (ancien = null).
        if (assignee != null) {
            notificationService.notifyAssignment(assignee, task);
        }

        return convertToDto(task);
    }

    /**
     * Modifie une tâche (ADMINISTRATOR et MEMBER). Trace chaque champ modifié
     * et notifie l'assigné uniquement si l'assignation a changé.
     *
     * @param userId    identifiant de l'auteur
     * @param projectId identifiant du projet
     * @param taskId    identifiant de la tâche
     * @param request   nouvelles données de la tâche
     * @return tâche modifiée sous forme de DTO
     */
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
        Long oldAssigneeId = oldAssignee != null ? oldAssignee.getId() : null;

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

        // Notification uniquement si un assigné est défini ET qu'il a changé.
        if (assignee != null && !Objects.equals(oldAssigneeId, assignee.getId())) {
            notificationService.notifyAssignment(assignee, task);
        }

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

    /**
     * Refuse l'action aux observateurs (VIEWER).
     */
    private void requireCanWrite(ProjectMember actor) {
        if (actor.getRole() == ProjectRole.VIEWER) {
            throw new ForbiddenException("Les observateurs ne peuvent pas modifier les tâches.");
        }
    }

    /**
     * Retourne l'appartenance de l'utilisateur au projet, ou lève une exception 403/404.
     */
    private ProjectMember getMembershipOrThrow(Long userId, Project project) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
        return projectMemberRepository.findByProjectAndUser(project, user)
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes pas membre de ce projet."));
    }

    /**
     * Retourne la tâche si elle appartient bien au projet, sinon lève une exception 404.
     */
    private Task getTaskInProject(Long taskId, Project project) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getProject().getId().equals(project.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée dans ce projet."));
    }

    /**
     * Retourne le projet ou lève une exception 404.
     */
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé."));
    }

    /**
     * Convertit une entité Task en DTO exposé par l'API.
     */
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