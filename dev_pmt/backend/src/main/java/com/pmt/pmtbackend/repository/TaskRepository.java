package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.model.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'entité Task.
 * Permet de gérer les opérations CRUD et les recherches de tâches.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Récupère toutes les tâches d'un projet donné.
     * @param project le projet
     * @return liste des tâches du projet
     */
    List<Task> findByProject(Project project);

    /**
     * Récupère les tâches assignées à un utilisateur ayant un statut spécifique.
     * Utile pour filtrer les tâches en cours ou à faire d'un assigné.
     * @param assignee l'utilisateur assigné
     * @param status le statut de la tâche
     * @return liste des tâches correspondant aux critères
     */
    List<Task> findByAssigneeAndStatus(User assignee, TaskStatus status);
}