package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.model.entity.History;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.repository.HistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/** Vérifie l'enregistrement conditionnel de l'historique. */
@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private HistoryRepository historyRepository;

    @InjectMocks
    private HistoryService historyService;

    @Test
    // Vérifie qu'une valeur modifiée est enregistrée.
    void recordEnregistreUneValeurModifiee() {
        Task task = new Task();
        User author = new User();

        historyService.record("status", "A_FAIRE", "FAIT", task, author);

        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);
        verify(historyRepository).save(captor.capture());
        assertEquals("status", captor.getValue().getModifiedField());
        assertEquals("A_FAIRE", captor.getValue().getOldValue());
        assertEquals("FAIT", captor.getValue().getNewValue());
    }

    @Test
    // Vérifie qu'une valeur identique n'ajoute pas d'historique.
    void recordNEnregistrePasUneValeurIdentique() {
        historyService.record("status", null, null, new Task(), new User());

        verifyNoInteractions(historyRepository);
    }
}
