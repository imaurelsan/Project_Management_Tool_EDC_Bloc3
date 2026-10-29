package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.model.entity.History;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.HistoryRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Service chargé d'enregistrer les modifications apportées aux tâches.
 * Chaque changement de champ produit une ligne d'historique : champ, ancienne
 * valeur, nouvelle valeur, auteur. La date est posée automatiquement par l'entité.
 */
@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * Enregistre une modification si, et seulement si, la valeur a réellement changé.
     *
     * @param field    nom du champ modifié (ex. "status")
     * @param oldValue ancienne valeur (null à la création)
     * @param newValue nouvelle valeur (null si le champ est vidé)
     * @param task     tâche concernée
     * @param author   utilisateur qui a effectué la modification
     */
    public void record(String field, Object oldValue, Object newValue, Task task, User author) {
        if (Objects.equals(oldValue, newValue)) {
            return; // rien n'a changé, on ne pollue pas l'historique
        }
        historyRepository.save(new History(
                field,
                oldValue != null ? oldValue.toString() : null,
                newValue != null ? newValue.toString() : null,
                task,
                author));
    }
}