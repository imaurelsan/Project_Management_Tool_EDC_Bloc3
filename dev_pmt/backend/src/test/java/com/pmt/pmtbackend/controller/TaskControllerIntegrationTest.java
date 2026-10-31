package com.pmt.pmtbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Vérifie les routes des tâches avec une base H2. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMemberRepository projectMemberRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private NotificationRepository notificationRepository;

    @MockBean
    private JavaMailSender mailSender;

    private User admin;
    private User member;
    private User viewer;
    private Project project;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        admin = user("admin", "admin@test.local");
        member = user("member", "member@test.local");
        viewer = user("viewer", "viewer@test.local");
        userRepository.saveAll(List.of(admin, member, viewer));

        project = projectRepository.save(new Project("Projet", "Description", admin));
        projectMemberRepository.save(new ProjectMember(project, admin, ProjectRole.ADMINISTRATOR));
        projectMemberRepository.save(new ProjectMember(project, member, ProjectRole.MEMBER));
        projectMemberRepository.save(new ProjectMember(project, viewer, ProjectRole.VIEWER));
    }

    @Test
    // Vérifie création, modification, historique et suppression selon le rôle.
    void creationModificationSuppressionEtHistoriqueFonctionnent() throws Exception {
        String task = """
                {"title":"Tâche","description":"Description","status":"A_FAIRE","priority":"MOYEN","assigneeId":%d}
                """.formatted(member.getId());

        mockMvc.perform(post("/projects/{id}/tasks", project.getId())
                        .header("X-User-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(task))
                .andExpect(status().isCreated());

        Task created = taskRepository.findByProject(project).get(0);
        mockMvc.perform(put("/projects/{projectId}/tasks/{taskId}", project.getId(), created.getId())
                        .header("X-User-Id", member.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(task.replace("Tâche", "Tâche modifiée")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/{projectId}/tasks/{taskId}/history", project.getId(), created.getId())
                        .header("X-User-Id", viewer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mockMvc.perform(delete("/projects/{projectId}/tasks/{taskId}", project.getId(), created.getId())
                        .header("X-User-Id", member.getId()))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/projects/{projectId}/tasks/{taskId}", project.getId(), created.getId())
                        .header("X-User-Id", admin.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    // Vérifie le rejet des champs obligatoires manquants.
    void validationRefuseStatusEtPriorityManquants() throws Exception {
        String invalidTask = """
                {"title":"Tâche","description":"Description"}
                """;

        mockMvc.perform(post("/projects/{id}/tasks", project.getId())
                        .header("X-User-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(invalidTask))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.status").exists())
                .andExpect(jsonPath("$.errors.priority").exists());
    }

    @Test
    // Vérifie la notification enregistrée et le mail envoyé à l'assigné.
    void assignationCreeUneNotificationLisibleParRepository() throws Exception {
        String task = """
                {"title":"Notification","description":"Description","status":"A_FAIRE","priority":"HAUTE","assigneeId":%d}
                """.formatted(member.getId());

        mockMvc.perform(post("/projects/{id}/tasks", project.getId())
                        .header("X-User-Id", admin.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(task))
                .andExpect(status().isCreated());

        List<com.pmt.pmtbackend.model.entity.Notification> notifications =
                notificationRepository.findByUserOrderByCreatedAtDesc(member);
        org.junit.jupiter.api.Assertions.assertEquals(1, notifications.size());
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    private static User user(String username, String email) {
        return new User(username, email, "password");
    }
}
