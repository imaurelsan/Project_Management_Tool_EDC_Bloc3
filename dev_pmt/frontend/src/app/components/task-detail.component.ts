import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProjectService } from '../services/project.service';
import { History, Task } from '../models';
import { apiErrorMessage } from '../shared/api-error';
import { finalize } from 'rxjs';

/**
 * Affiche une tâche et l'historique de ses modifications.
 */
@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <a [routerLink]="['/projects', projectId]">← Retour au projet</a>
    @if (loading()) { <p>Chargement de la tâche...</p> }
    @if (error()) { <p class="error">{{ error() }}</p> }
    @if (task()) {
      <section class="page">
        <h1>{{ task()!.title }}</h1>
        <p>{{ task()!.description }}</p>
        <p>{{ task()!.status }} · {{ task()!.priority }} · {{ task()!.dueDate || 'Sans échéance' }}</p>
        <h2>Historique</h2>
        @if (historyLoading()) { <p>Chargement de l'historique...</p> }
        <ul>
          @for (entry of history(); track entry.id) {
            <li>{{ entry.modifiedDate }} — {{ entry.modifiedBy }} : {{ entry.modifiedField }}
              ({{ entry.oldValue || 'vide' }} → {{ entry.newValue || 'vide' }})</li>
          } @empty { <li>Aucune modification enregistrée.</li> }
        </ul>
      </section>
    }
  `,
})
export class TaskDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(ProjectService);
  projectId = 0;
  task = signal<Task | null>(null);
  history = signal<History[]>([]);
  loading = signal(true);
  historyLoading = signal(true);
  error = signal('');

  ngOnInit(): void {
    this.projectId = Number(this.route.snapshot.paramMap.get('projectId'));
    const taskId = Number(this.route.snapshot.paramMap.get('taskId'));
    if (!Number.isInteger(this.projectId) || this.projectId <= 0 ||
        !Number.isInteger(taskId) || taskId <= 0) {
      this.error.set('La tâche demandée est invalide.');
      this.loading.set(false);
      this.historyLoading.set(false);
      return;
    }

    this.service.getTask(this.projectId, taskId).pipe(finalize(() => {
      this.loading.set(false);
    })).subscribe({
      next: task => {
        this.task.set(task);
      },
      error: error => {
        this.error.set(apiErrorMessage(error));
      },
    });
    this.service.history(this.projectId, taskId).pipe(finalize(() => {
      this.historyLoading.set(false);
    })).subscribe({
      next: history => {
        this.history.set(history);
      },
      error: error => {
        this.error.set(apiErrorMessage(error));
      },
    });
  }
}
