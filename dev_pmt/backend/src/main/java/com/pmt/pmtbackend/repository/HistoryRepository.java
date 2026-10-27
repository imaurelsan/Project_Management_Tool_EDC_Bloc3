package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.History;
import com.pmt.pmtbackend.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Accès aux données de l'historique des modifications de tâches.
 */
@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    /**
     * Retourne l'historique d'une tâche, de la modification la plus récente
     * à la plus ancienne.
     *
     * @param task la tâche concernée
     * @return la liste des entrées d'historique
     */
    List<History> findByTaskOrderByModifiedDateDesc(Task task);
}