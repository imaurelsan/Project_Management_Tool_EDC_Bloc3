package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Vérifie les droits selon le rôle dans un projet. */
@ExtendWith(MockitoExtension.class)
class ProjectAccessPolicyTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectAccessPolicy accessPolicy;

    @Test
    // Vérifie la récupération du rôle d'un membre.
    void roleOfRetourneLeRoleDuMembre() {
        User user = new User();
        Project project = new Project();
        when(projectMemberRepository.findByProjectAndUser(project, user))
                .thenReturn(Optional.of(new ProjectMember(project, user, ProjectRole.MEMBER)));

        assertEquals(ProjectRole.MEMBER, accessPolicy.roleOf(user, project));
    }

    @Test
    // Vérifie le refus pour un utilisateur non membre.
    void roleOfRefuseUnNonMembre() {
        User user = new User();
        Project project = new Project();
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> accessPolicy.roleOf(user, project));
    }

    @Test
    // Vérifie le refus d'un observateur et l'accès d'un membre.
    void requireWriteAccessRefuseViewerEtAccepteMember() {
        User user = new User();
        Project project = new Project();
        ProjectMember membership = new ProjectMember(project, user, ProjectRole.VIEWER);
        when(projectMemberRepository.findByProjectAndUser(project, user)).thenReturn(Optional.of(membership));

        assertThrows(ForbiddenException.class, () -> accessPolicy.requireWriteAccess(user, project));
        membership.setRole(ProjectRole.MEMBER);
        accessPolicy.requireWriteAccess(user, project);
    }

    @Test
    // Vérifie que seul un administrateur peut gérer le projet.
    void requireAdministratorRefuseMember() {
        User user = new User();
        Project project = new Project();
        when(projectMemberRepository.findByProjectAndUser(project, user))
                .thenReturn(Optional.of(new ProjectMember(project, user, ProjectRole.MEMBER)));

        assertThrows(ForbiddenException.class, () -> accessPolicy.requireAdministrator(user, project));
    }
}
