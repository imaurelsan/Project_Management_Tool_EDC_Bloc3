package com.pmt.pmtbackend.exception;

/**
 * Exception non vérifiée levée lorsqu'une ressource existe déjà dans le système.
 * Cette exception est utilisée pour indiquer une erreur HTTP 409 (Conflict).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}