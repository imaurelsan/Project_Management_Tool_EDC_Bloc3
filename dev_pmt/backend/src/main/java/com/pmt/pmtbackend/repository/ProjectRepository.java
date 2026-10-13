package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.Project;
import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'entité Project.
 * Permet de gérer les opérations CRUD et les recherches de projets.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Récupère tous les projets créés par un utilisateur donné.
     * @param createdBy l'utilisateur créateur
     * @return liste des projets créés par cet utilisateur
     */
    List<Project> findByCreatedBy(User createdBy);

    /**
     * Recherche les projets par partie de nom (insensible à la casse).
     * @param name la portion du nom à rechercher
     * @return liste des projets correspondant à la recherche
     */
    List<Project> findByNameContainingIgnoreCase(String name);
}