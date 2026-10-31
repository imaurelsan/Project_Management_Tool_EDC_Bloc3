package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.model.entity.Notification;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/** Vérifie la création d'une notification et l'envoi du mail. */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    // Vérifie l'enregistrement et l'envoi d'un mail.
    void notifyAssignmentEnregistreEtEnvoieUnMail() {
        User user = user(2L, "member@test.local");
        Task task = task("Tâche test");

        notificationService = new NotificationService(notificationRepository, mailSender, "pmt@test.local");
        notificationService.notifyAssignment(user, task);

        verify(notificationRepository).save(any(Notification.class));
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    @Test
    // Vérifie qu'une panne SMTP ne supprime pas la notification.
    void notifyAssignmentConserveLaNotificationSiLeMailEchoue() {
        User user = user(2L, "member@test.local");
        Task task = task("Tâche test");
        doThrow(new RuntimeException("SMTP indisponible")).when(mailSender)
                .send(any(org.springframework.mail.SimpleMailMessage.class));

        notificationService = new NotificationService(notificationRepository, mailSender, "pmt@test.local");
        notificationService.notifyAssignment(user, task);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        verify(mailSender).send(any(org.springframework.mail.SimpleMailMessage.class));
    }

    private static User user(Long id, String email) {
        User user = new User("member", email, "password");
        user.setId(id);
        return user;
    }

    private static Task task(String title) {
        Project project = new Project();
        project.setName("Projet");
        Task task = new Task();
        task.setTitle(title);
        task.setProject(project);
        return task;
    }
}
