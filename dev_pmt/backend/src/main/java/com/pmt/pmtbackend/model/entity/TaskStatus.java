package com.pmt.pmtbackend.model.entity;

/**
 * Énumération des états possibles d'une tâche.
 * Représente le cycle de vie d'une tâche dans le projet.
 *
 * États disponibles :
 * - A_FAIRE : Tâche créée, en attente de démarrage
 * - EN_COURS : Tâche actuellement en cours de réalisation
 * - FAIT : Tâche terminée avec succès
 * - ANNULE : Tâche annulée ou abandonnée
 *
 * @see Task pour l'utilisation de cet enum
 */
public enum TaskStatus {
    A_FAIRE("À faire"),
    EN_COURS("En cours"),
    FAIT("Fait"),
    ANNULE("Annulé");

    private final String displayName;

    /**
     * Constructeur pour initialiser le nom d'affichage français.
     * @param displayName le libellé en français
     */
    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retourne le nom d'affichage en français de cet état.
     * Utilisé pour l'interface utilisateur et les rapports.
     * @return le libellé français
     */
    public String getDisplayName() {
        return displayName;
    }
}