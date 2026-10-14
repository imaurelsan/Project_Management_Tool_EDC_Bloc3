package com.pmt.pmtbackend.exception;

/**
 * Exception non vérifiée levée lorsqu'une ressource demandée n'est pas trouvée.
 * Cette exception est utilisée pour indiquer une erreur HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}