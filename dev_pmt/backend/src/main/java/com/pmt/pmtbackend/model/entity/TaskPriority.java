package com.pmt.pmtbackend.model.entity;

/**
 * Énumération des niveaux de priorité possibles pour une tâche.
 * La priorité aide à organiser l'ordre d'exécution et l'urgence des tâches.
 *
 * Niveaux disponibles :
 * - FAIBLE : Tâche non urgente, peut être reportée
 * - MOYEN : Priorité normale, traiter en temps normal
 * - HAUTE : Priorité élevée, traiter rapidement
 * - CRITIQUE : Priorité critique, traiter immédiatement
 *
 * @see Task pour l'utilisation de cet enum
 */
public enum TaskPriority {
    FAIBLE("Faible"),
    MOYEN("Moyen"),
    HAUTE("Haute"),
    CRITIQUE("Critique");

    private final String displayName;

    /**
     * Constructeur pour initialiser le nom d'affichage français.
     * @param displayName le libellé en français
     */
    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retourne le nom d'affichage en français de ce niveau de priorité.
     * Utilisé pour l'interface utilisateur et les filtres.
     * @return le libellé français
     */
    public String getDisplayName() {
        return displayName;
    }
}