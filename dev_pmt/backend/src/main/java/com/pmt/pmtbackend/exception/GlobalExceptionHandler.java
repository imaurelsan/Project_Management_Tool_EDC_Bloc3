package com.pmt.pmtbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions de l'API.
 * Convertit les exceptions métier et les erreurs de validation en réponses HTTP
 * au format JSON homogène.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traite une ressource introuvable (HTTP 404).
     *
     * @param ex l'exception levée
     * @return la réponse d'erreur
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Traite une ressource déjà existante (HTTP 409).
     *
     * @param ex l'exception levée
     * @return la réponse d'erreur
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Traite une action interdite pour l'utilisateur courant (HTTP 403).
     *
     * @param ex l'exception levée
     * @return la réponse d'erreur
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Traite une authentification échouée (HTTP 401).
     *
     * @param ex l'exception levée
     * @return la réponse d'erreur
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Traite les erreurs de validation des DTO annotés Bean Validation (HTTP 400).
     * Retourne le détail champ par champ.
     *
     * @param ex l'exception levée
     * @return la réponse d'erreur avec la liste des champs invalides
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Données invalides.");
        body.put("errors", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Construit le corps standard d'une réponse d'erreur.
     *
     * @param status le statut HTTP
     * @param message le message à renvoyer au client
     * @return la réponse d'erreur
     */
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    /**
     * Crée la structure commune de toutes les réponses d'erreur.
     *
     * @param status le statut HTTP
     * @param message le message
     * @return une map mutable contenant horodatage, statut et message
     */
    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}