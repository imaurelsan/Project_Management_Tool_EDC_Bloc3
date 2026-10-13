package com.pmt.pmtbackend.model.entity;

/**
 * Énumération des rôles disponibles dans un projet.
 * Chaque rôle détermine les permissions de l'utilisateur dans le contexte du projet.
 *
 * Rôles disponibles :
 * - ADMINISTRATOR : Accès complet au projet (édition, suppression, gestion des membres)
 * - MEMBER : Accès standard (création et édition de tâches, consultation)
 * - VIEWER : Accès lecture seule (consultation uniquement)
 *
 * @see ProjectMember pour l'association utilisateur-projet-rôle
 */
public enum ProjectRole {
    ADMINISTRATOR("Administrateur"),
    MEMBER("Membre"),
    VIEWER("Observateur");

    private final String displayName;

    /**
     * Constructeur pour initialiser le nom d'affichage français.
     * @param displayName le libellé en français
     */
    ProjectRole(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retourne le nom d'affichage en français de ce rôle.
     * Utilisé pour l'interface utilisateur.
     * @return le libellé français
     */
    public String getDisplayName() {
        return displayName;
    }
}