package com.pmt.pmtbackend.exception;


/**
 * Exception non vérifiée levée lorsqu'une action est interdite pour l'utilisateur actuel.
 * Cette exception est utilisée pour indiquer une erreur HTTP 403 (Forbidden).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}