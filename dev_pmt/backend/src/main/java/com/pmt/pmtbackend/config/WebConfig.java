package com.pmt.pmtbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise l'application Angular locale à appeler l'API REST.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure les origines et méthodes utilisées par le frontend.
     *
     * @param registry registre de configuration CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
