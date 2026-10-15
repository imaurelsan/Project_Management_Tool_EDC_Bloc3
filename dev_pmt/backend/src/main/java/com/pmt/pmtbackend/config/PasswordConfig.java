package com.pmt.pmtbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration pour le chiffrement des mots de passe.
 * Cette classe fournit un bean PasswordEncoder basé sur BCrypt.
 */
@Configuration
public class PasswordConfig {

    /**
     * Crée et configure un encodeur de mot de passe utilisant l'algorithme BCrypt.
     * @return instance de PasswordEncoder configurée
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}