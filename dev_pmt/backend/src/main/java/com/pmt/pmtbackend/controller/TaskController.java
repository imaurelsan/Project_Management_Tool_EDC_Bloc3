package com.pmt.pmtbackend.controller;

import com.pmt.pmtbackend.dto.HistoryDto;
import com.pmt.pmtbackend.dto.TaskDto;
import com.pmt.pmtbackend.dto.TaskRequest;
import com.pmt.pmtbackend.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Routes REST des tâches d'un projet.
 */
@RestController
@RequestMapping("/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskDto> getTasks(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long projectId) {
        return taskService.getTasks(userId, projectId);
    }

    @GetMapping("/{taskId}")
    public TaskDto getTask(@RequestHeader("X-User-Id") Long userId,
                           @PathVariable Long projectId,
                           @PathVariable Long taskId) {
        return taskService.getTask(userId, projectId, taskId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@RequestHeader("X-User-Id") Long userId,
                          @PathVariable Long projectId,
                          @Valid @RequestBody TaskRequest request) {
        return taskService.create(userId, projectId, request);
    }

    @PutMapping("/{taskId}")
    public TaskDto update(@RequestHeader("X-User-Id") Long userId,
                          @PathVariable Long projectId,
                          @PathVariable Long taskId,
                          @Valid @RequestBody TaskRequest request) {
        return taskService.update(userId, projectId, taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Id") Long userId,
                       @PathVariable Long projectId,
                       @PathVariable Long taskId) {
        taskService.delete(userId, projectId, taskId);
    }

    /**
     * GET /api/projects/{projectId}/tasks/{taskId}/history
     * Retourne l'historique des modifications d'une tâche (tous les rôles).
     * Réponses : 200 liste, 403 non membre, 404 tâche introuvable.
     */
    @GetMapping("/{taskId}/history")
    public List<HistoryDto> getHistory(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable Long projectId,
                                       @PathVariable Long taskId) {
        return taskService.getHistory(userId, projectId, taskId);
    }
}