package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.dto.AddMemberRequest;
import com.pmt.pmtbackend.dto.ProjectRequest;
import com.pmt.pmtbackend.dto.UpdateMemberRoleRequest;
import com.pmt.pmtbackend.exception.DuplicateResourceException;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.model.entity.*;
import com.pmt.pmtbackend.repository.ProjectMemberRepository;
import com.pmt.pmtbackend.repository.ProjectRepository;
import com.pmt.pmtbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/** Vérifie la gestion des projets et de leurs membres. */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectAccessPolicy accessPolicy;
    @InjectMocks private ProjectService projectService;

    private User owner;
    private User member;
    private Project project;

    @BeforeEach
    void setUp() {
        owner = new User("owner", "owner@test.local", "password");
        owner.setId(1L);
        member = new User("member", "member@test.local", "password");
        member.setId(2L);
        project = new Project("Projet", "Description", owner);
        project.setId(1L);
    }

    @Test
    // Vérifie la création du projet et de son administrateur.
    void createAjouteLeProprietaireCommeAdministrateur() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        projectService.create(1L, new ProjectRequest("Nouveau", "Description"));

        verify(projectRepository).save(any(Project.class));
        verify(projectMemberRepository).save(argThat(m -> m.getUser() == owner
                && m.getRole() == ProjectRole.ADMINISTRATOR));
    }

    @Test
    // Vérifie le refus d'accès pour un utilisateur non membre.
    void getByIdRefuseUnUtilisateurNonMembre() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        doThrow(new ForbiddenException("interdit")).when(accessPolicy).requireMembership(member, project);

        assertThrows(ForbiddenException.class, () -> projectService.getById(2L, 1L));
    }

    @Test
    // Vérifie qu'un utilisateur ne peut pas être ajouté deux fois.
    void addMemberRefuseUnDoublon() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByEmail("member@test.local")).thenReturn(Optional.of(member));
        when(accessPolicy.findMembership(member, project))
                .thenReturn(Optional.of(new ProjectMember(project, member, ProjectRole.MEMBER)));
        doNothing().when(accessPolicy).requireAdministrator(owner, project);

        assertThrows(DuplicateResourceException.class,
                () -> projectService.addMember(1L, 1L,
                        new AddMemberRequest(" MEMBER@TEST.LOCAL ", ProjectRole.MEMBER)));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    // Vérifie la protection du dernier administrateur.
    void updateRoleRefuseDegraderLeDernierAdministrateur() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(userRepository.findById(2L)).thenReturn(Optional.of(member));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(accessPolicy.findMembership(member, project))
                .thenReturn(Optional.of(new ProjectMember(project, member, ProjectRole.ADMINISTRATOR)));
        when(projectMemberRepository.findByProjectAndRole(project, ProjectRole.ADMINISTRATOR))
                .thenReturn(List.of(new ProjectMember(project, owner, ProjectRole.ADMINISTRATOR)));

        assertThrows(ForbiddenException.class, () -> projectService.updateMemberRole(
                1L, 1L, 2L, new UpdateMemberRoleRequest(ProjectRole.MEMBER)));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    // Vérifie l'erreur pour un utilisateur absent.
    void getAllForUserEchoueSiUtilisateurAbsent() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getAllForUser(99L));
    }
}
