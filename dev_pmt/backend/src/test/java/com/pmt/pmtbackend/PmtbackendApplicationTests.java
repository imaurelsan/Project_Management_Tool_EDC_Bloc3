package com.pmt.pmtbackend;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

/** Vérifie que le contexte Spring démarre avec la configuration de test. */
@SpringBootTest
@ActiveProfiles("test")
class PmtbackendApplicationTests {

	@Test
	// Vérifie le chargement complet de l'application.
	void contextLoads() {
	}

}
