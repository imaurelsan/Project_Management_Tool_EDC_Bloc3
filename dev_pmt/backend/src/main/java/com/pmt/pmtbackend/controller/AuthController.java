package com.pmt.pmtbackend.controller;

import com.pmt.pmtbackend.dto.LoginRequest;
import com.pmt.pmtbackend.dto.RegisterRequest;
import com.pmt.pmtbackend.dto.UserDto;
import com.pmt.pmtbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour l'inscription et la connexion des utilisateurs.
 * Les erreurs métier sont traitées par GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    /**
     * Constructeur avec injection du service utilisateur.
     *
     * @param userService le service de gestion des utilisateurs
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Inscrit un nouvel utilisateur.
     *
     * @param request les données d'inscription validées
     * @return HTTP 201 avec l'utilisateur créé
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Connecte un utilisateur à partir de son e-mail et de son mot de passe.
     *
     * @param request les identifiants validés
     * @return HTTP 200 avec l'utilisateur connecté
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginRequest request) {
        UserDto user = userService.login(request);
        return ResponseEntity.ok(user);
    }
}