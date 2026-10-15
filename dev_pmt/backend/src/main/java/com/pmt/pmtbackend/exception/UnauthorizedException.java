package com.pmt.pmtbackend.exception;

/**
 * Exception levée lorsqu'une tentative d'accès non autorisée est faite.
 * Cette exception sera traduite en HTTP 401 (Non autorisé).
 */
public class UnauthorizedException extends RuntimeException {
    /**
     * Constructeur avec message.
     *
     * @param message Le message de l'exception.
     */
    public UnauthorizedException(String message) {
        super(message);
    }
}