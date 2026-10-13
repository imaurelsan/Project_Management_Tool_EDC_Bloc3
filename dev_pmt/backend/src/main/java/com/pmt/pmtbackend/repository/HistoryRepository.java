package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.History;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour l'entité History.
 * Permet de gérer l'historique des modifications ou actions dans le système.
 */
@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {

    /**
     * Récupère l'historique d'une tâche donnée.
     * @param task la tâche concernée
     * @return liste des modifications ou actions liées à cette tâche
     */
    List<History> findByTask(Task task);

    /**
     * Récupère l'historique d'un utilisateur donné.
     * @param user l'utilisateur concerné
     * @return liste des modifications ou actions liées à cet utilisateur
     */
    List<History> findByModifiedBy(User user);
}