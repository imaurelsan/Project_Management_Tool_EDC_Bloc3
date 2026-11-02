package com.pmt.pmtbackend.controller;

import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Vérifie la consultation et la lecture des notifications personnelles. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectMemberRepository projectMemberRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private NotificationRepository notificationRepository;

    private User firstUser;
    private User secondUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        firstUser = userRepository.save(new User("first", "first@test.local", "password"));
        secondUser = userRepository.save(new User("second", "second@test.local", "password"));
        Project project = projectRepository.save(new Project("Projet", "Description", firstUser));
        projectMemberRepository.save(new ProjectMember(project, firstUser, ProjectRole.ADMINISTRATOR));
        Task task = taskRepository.save(new Task("Tâche", "Description", TaskStatus.A_FAIRE,
                TaskPriority.MOYEN, null, project, firstUser));
        notification = notificationRepository.save(new Notification(firstUser, task, "Nouvelle tâche"));
    }

    @Test
    // Vérifie que chaque utilisateur ne reçoit que ses propres notifications.
    void listeLesNotificationsDuCompteCourant() throws Exception {
        notificationRepository.save(new Notification(secondUser, notification.getTask(), "Autre notification"));

        mockMvc.perform(get("/notifications").header("X-User-Id", firstUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(org.hamcrest.Matchers.hasSize(1))))
                .andExpect(jsonPath("$[0].message").value("Nouvelle tâche"))
                .andExpect(jsonPath("$[0].projectId").value(notification.getTask().getProject().getId()))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    // Vérifie la lecture autorisée et le refus d'une notification étrangère.
    void marqueSeulementUneNotificationQuiAppartientAuCompte() throws Exception {
        mockMvc.perform(patch("/notifications/{id}/read", notification.getId())
                        .header("X-User-Id", firstUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/notifications/{id}/read", notification.getId())
                        .header("X-User-Id", secondUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    // Vérifie que le navigateur peut préparer une requête PATCH depuis Angular.
    void accepteLePrevolCorsPourPatch() throws Exception {
        mockMvc.perform(options("/notifications/{id}/read", notification.getId())
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "X-User-Id,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods",
                        org.hamcrest.Matchers.containsString("PATCH")));
    }
}
