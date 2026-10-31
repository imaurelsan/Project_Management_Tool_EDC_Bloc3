package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.TaskRequest;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Vérifie les règles principales du service des tâches. */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private HistoryRepository historyRepository;
    @Mock private HistoryService historyService;
    @Mock private NotificationService notificationService;
    @InjectMocks private TaskService taskService;

    private User author;
    private User assignee;
    private Project project;

    @BeforeEach
    void setUp() {
        author = new User("author", "author@test.local", "password");
        author.setId(1L);
        assignee = new User("assignee", "assignee@test.local", "password");
        assignee.setId(2L);
        project = new Project("Projet", "Description", author);
        project.setId(1L);
    }

    @Test
    // Vérifie la notification lors d'une première assignation.
    void createAvecAssigneNotifieApresSauvegarde() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(projectMemberRepository.findByProjectAndUser(project, author))
                .thenReturn(Optional.of(new ProjectMember(project, author, ProjectRole.ADMINISTRATOR)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.findByProjectAndUser(project, assignee))
                .thenReturn(Optional.of(new ProjectMember(project, assignee, ProjectRole.MEMBER)));

        taskService.create(1L, 1L, request(2L));

        verify(taskRepository).save(any(Task.class));
        verify(notificationService).notifyAssignment(eq(assignee), any(Task.class));
    }

    @Test
    // Vérifie qu'une assignation inchangée ne renvoie pas de notification.
    void updateAvecMemeAssigneNeNotifiePas() {
        Task task = task(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(projectMemberRepository.findByProjectAndUser(project, author))
                .thenReturn(Optional.of(new ProjectMember(project, author, ProjectRole.ADMINISTRATOR)));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.findByProjectAndUser(project, assignee))
                .thenReturn(Optional.of(new ProjectMember(project, assignee, ProjectRole.MEMBER)));

        taskService.update(1L, 1L, 2L, request(2L));

        verify(taskRepository).save(task);
        verifyNoInteractions(notificationService);
    }

    @Test
    // Vérifie qu'un observateur ne peut pas créer de tâche.
    void createRefuseUnObservateur() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(projectMemberRepository.findByProjectAndUser(project, author))
                .thenReturn(Optional.of(new ProjectMember(project, author, ProjectRole.VIEWER)));

        assertThrows(ForbiddenException.class, () -> taskService.create(1L, 1L, request(null)));
        verifyNoInteractions(taskRepository, notificationService);
    }

    @Test
    // Vérifie l'erreur lorsqu'un projet est introuvable.
    void getTaskEchoueSiProjetIntrouvable() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTask(1L, 99L, 1L));
    }

    @Test
    // Vérifie qu'une tâche sans assigné ne crée pas de notification.
    void createSansAssigneNeNotifiePas() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(projectMemberRepository.findByProjectAndUser(project, author))
                .thenReturn(Optional.of(new ProjectMember(project, author, ProjectRole.ADMINISTRATOR)));

        taskService.create(1L, 1L, request(null));

        verify(taskRepository).save(any(Task.class));
        verifyNoInteractions(notificationService);
    }

    @Test
    // Vérifie la notification lorsqu'un nouvel assigné est choisi.
    void updateAvecNouvelAssigneNotifie() {
        Task task = task(2L);
        task.setAssignee(null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(projectMemberRepository.findByProjectAndUser(project, author))
                .thenReturn(Optional.of(new ProjectMember(project, author, ProjectRole.ADMINISTRATOR)));
        when(taskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.findByProjectAndUser(project, assignee))
                .thenReturn(Optional.of(new ProjectMember(project, assignee, ProjectRole.MEMBER)));

        taskService.update(1L, 1L, 2L, request(2L));

        verify(notificationService).notifyAssignment(eq(assignee), eq(task));
    }

    private TaskRequest request(Long assigneeId) {
        return new TaskRequest("Tâche", "Description", TaskStatus.A_FAIRE, TaskPriority.MOYEN, null, assigneeId);
    }

    private Task task(Long id) {
        Task task = new Task("Tâche", "Description", TaskStatus.A_FAIRE, TaskPriority.MOYEN, null, project, assignee);
        task.setId(id);
        return task;
    }
}
