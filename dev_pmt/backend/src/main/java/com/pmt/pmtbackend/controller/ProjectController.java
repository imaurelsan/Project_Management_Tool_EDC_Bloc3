package com.pmt.pmtbackend.controller;

import com.pmt.pmtbackend.dto.AddMemberRequest;
import com.pmt.pmtbackend.dto.ProjectDto;
import com.pmt.pmtbackend.dto.ProjectMemberDto;
import com.pmt.pmtbackend.dto.ProjectRequest;
import com.pmt.pmtbackend.dto.UpdateMemberRoleRequest;
import com.pmt.pmtbackend.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des projets et de leurs membres.
 * L'identifiant de l'utilisateur courant est fourni par l'en-tête X-User-Id
 * (solution provisoire, remplacée par l'authentification dans un lot dédié).
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Constructeur avec injection du service projet.
     *
     * @param projectService le service de gestion des projets
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Crée un projet dont l'utilisateur courant devient propriétaire.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param request les données du projet
     * @return HTTP 201 avec le projet créé
     */
    @PostMapping
    public ResponseEntity<ProjectDto> create(@RequestHeader("X-User-Id") Long userId,
                                             @Valid @RequestBody ProjectRequest request) {
        ProjectDto created = projectService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Liste les projets accessibles à l'utilisateur courant.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @return HTTP 200 avec la liste des projets
     */
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAll(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(projectService.getAllForUser(userId));
    }

    /**
     * Récupère un projet par son identifiant.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @return HTTP 200 avec le projet
     */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getById(@RequestHeader("X-User-Id") Long userId,
                                              @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getById(userId, projectId));
    }

    /**
     * Met à jour un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param request les nouvelles données
     * @return HTTP 200 avec le projet mis à jour
     */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDto> update(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable Long projectId,
                                             @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.update(userId, projectId, request));
    }

    /**
     * Supprime un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @return HTTP 204 sans contenu
     */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long projectId) {
        projectService.delete(userId, projectId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Liste les membres d'un projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @return HTTP 200 avec la liste des membres
     */
    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<ProjectMemberDto>> getMembers(@RequestHeader("X-User-Id") Long userId,
                                                             @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getMembers(userId, projectId));
    }

    /**
     * Ajoute un membre à un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param request l'e-mail du membre et son rôle
     * @return HTTP 201 avec le membre ajouté
     */
    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberDto> addMember(@RequestHeader("X-User-Id") Long userId,
                                                      @PathVariable Long projectId,
                                                      @Valid @RequestBody AddMemberRequest request) {
        ProjectMemberDto member = projectService.addMember(userId, projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * Modifie le rôle d'un membre. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param memberUserId l'identifiant de l'utilisateur membre à modifier
     * @param request le nouveau rôle
     * @return HTTP 200 avec le membre mis à jour
     */
    @PutMapping("/{projectId}/members/{memberUserId}")
    public ResponseEntity<ProjectMemberDto> updateMemberRole(@RequestHeader("X-User-Id") Long userId,
                                                             @PathVariable Long projectId,
                                                             @PathVariable Long memberUserId,
                                                             @Valid @RequestBody UpdateMemberRoleRequest request) {
        return ResponseEntity.ok(projectService.updateMemberRole(userId, projectId, memberUserId, request));
    }

    /**
     * Retire un membre d'un projet. Réservé aux administrateurs du projet.
     *
     * @param userId l'identifiant de l'utilisateur courant
     * @param projectId l'identifiant du projet
     * @param memberUserId l'identifiant de l'utilisateur membre à retirer
     * @return HTTP 204 sans contenu
     */
    @DeleteMapping("/{projectId}/members/{memberUserId}")
    public ResponseEntity<Void> removeMember(@RequestHeader("X-User-Id") Long userId,
                                             @PathVariable Long projectId,
                                             @PathVariable Long memberUserId) {
        projectService.removeMember(userId, projectId, memberUserId);
        return ResponseEntity.noContent().build();
    }
}