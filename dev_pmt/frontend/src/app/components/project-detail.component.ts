import { ChangeDetectorRef, Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { ProjectService } from '../services/project.service';
import { Member, Project, ProjectRole, Task, TaskStatus } from '../models';
import { apiErrorMessage } from '../shared/api-error';
import { TaskFormComponent } from './task-form.component';
import { finalize } from 'rxjs';

/**
 * Affiche un projet, ses membres, ses tâches et son tableau de bord.
 */
@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TaskFormComponent],
  template: `
    @if (loading()) { <p>Chargement du projet...</p> }
    @if (error()) { <p class="error">{{ error() }}</p> }
    @if (project()) {
      <section class="page">
        <a routerLink="/projects">← Mes projets</a>
        <h1>{{ project()!.name }}</h1><p>{{ project()!.description }}</p>

        <div class="grid">
          <article class="card"><strong>{{ count('A_FAIRE') }}</strong><span>À faire</span></article>
          <article class="card"><strong>{{ count('EN_COURS') }}</strong><span>En cours</span></article>
          <article class="card"><strong>{{ count('FAIT') }}</strong><span>Terminées</span></article>
          <article class="card"><strong>{{ count('ANNULE') }}</strong><span>Annulées</span></article>
        </div>

        <h2>Membres</h2>
        <ul>
          @for (member of members(); track member.userId) {
            <li>
              {{ member.username }} — {{ member.email }}
              @if (isAdmin()) {
                <select [value]="member.role" (change)="changeRole(member, $event)">
                  <option value="ADMINISTRATOR">Administrateur</option>
                  <option value="MEMBER">Membre</option>
                  <option value="VIEWER">Observateur</option>
                </select>
              } @else { <span>({{ member.role }})</span> }
            </li>
          }
        </ul>
        @if (isAdmin()) {
          <form class="card form-grid" [formGroup]="memberForm" (ngSubmit)="addMember()">
            <input placeholder="E-mail du membre" formControlName="email">
            <select formControlName="role">
              <option value="MEMBER">Membre</option><option value="VIEWER">Observateur</option>
              <option value="ADMINISTRATOR">Administrateur</option>
            </select>
            <button type="submit">Ajouter</button>
          </form>
        }

        <div class="page-header">
          <h2>Tâches</h2>
          <select [value]="statusFilter()" (change)="filterTasks($event)">
            <option value="">Tous les statuts</option>
            @for (status of statuses; track status) { <option [value]="status">{{ status }}</option> }
          </select>
        </div>
        @if (!isViewer()) {
          <app-task-form [projectId]="project()!.id" [members]="members()" [task]="editingTask()"
                         (saved)="saveTask($event)" />
        }
        <div class="grid">
          @for (task of tasks(); track task.id) {
            <article class="card">
              <a [routerLink]="['/projects', project()!.id, 'tasks', task.id]"><strong>{{ task.title }}</strong></a>
              <span>{{ task.status }} · {{ task.priority }} · {{ task.assigneeUsername || 'Non assignée' }}</span>
              @if (!isViewer()) { <button type="button" (click)="editingTask.set(task)">Modifier</button> }
              @if (isAdmin()) { <button type="button" (click)="deleteTask(task)">Supprimer</button> }
            </article>
          } @empty { <p>Aucune tâche pour ce filtre.</p> }
        </div>
      </section>
    }
  `,
})
export class ProjectDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(ProjectService);
  private readonly auth = inject(AuthService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  readonly memberForm = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBER' as ProjectRole, Validators.required],
  });
  readonly statuses: TaskStatus[] = ['A_FAIRE', 'EN_COURS', 'FAIT', 'ANNULE'];
  project = signal<Project | null>(null);
  members = signal<Member[]>([]);
  tasks = signal<Task[]>([]);
  allTasks = signal<Task[]>([]);
  editingTask = signal<Task | null>(null);
  statusFilter = signal('');
  error = signal('');
  loading = signal(true);
  isAdmin = signal(false);
  isViewer = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('Le projet demandé est invalide.');
      this.loading.set(false);
      return;
    }

    this.service.get(id).pipe(finalize(() => {
      this.loading.set(false);
      this.changeDetector.markForCheck();
    })).subscribe({
      next: project => {
        this.project.set(project);
        this.changeDetector.markForCheck();
      },
      error: error => {
        this.error.set(apiErrorMessage(error));
        this.changeDetector.markForCheck();
      },
    });
    this.service.members(id).subscribe({
      next: members => {
        this.members.set(members);
        const current = members.find(member => member.userId === this.auth.currentUser()?.id);
        this.isAdmin.set(current?.role === 'ADMINISTRATOR');
        this.isViewer.set(current?.role === 'VIEWER');
      },
      error: error => this.error.set(apiErrorMessage(error)),
    });
    this.loadTasks(id);
  }

  loadTasks(projectId: number, status?: TaskStatus): void {
    this.service.tasks(projectId).subscribe({
      next: tasks => {
        this.allTasks.set(tasks);
        this.tasks.set(status ? tasks.filter(task => task.status === status) : tasks);
      },
      error: error => this.error.set(apiErrorMessage(error)),
    });
  }

  filterTasks(event: Event): void {
    this.statusFilter.set((event.target as HTMLSelectElement).value);
    this.loadTasks(this.project()!.id, (this.statusFilter() || undefined) as TaskStatus | undefined);
  }

  count(status: TaskStatus): number {
    return this.allTasks().filter(task => task.status === status).length;
  }

  addMember(): void {
    if (this.memberForm.invalid || !this.project()) {
      this.memberForm.markAllAsTouched();
      return;
    }
    const value = this.memberForm.getRawValue();
    this.service.addMember(this.project()!.id, value.email, value.role).subscribe({
      next: member => {
        this.members.update(members => [...members, member]);
        this.memberForm.reset({ email: '', role: 'MEMBER' });
      },
      error: error => this.error.set(apiErrorMessage(error)),
    });
  }

  changeRole(member: Member, event: Event): void {
    if (!this.project()) return;
    const role = (event.target as HTMLSelectElement).value as ProjectRole;
    this.service.updateMemberRole(this.project()!.id, member.userId, role).subscribe({
      next: updated => { member.role = updated.role; },
      error: error => this.error.set(apiErrorMessage(error)),
    });
  }

  saveTask(task: Task): void {
    this.allTasks.update(tasks => tasks.some(existing => existing.id === task.id)
      ? tasks.map(existing => existing.id === task.id ? task : existing)
      : [...tasks, task]);
    this.tasks.update(tasks => tasks.some(existing => existing.id === task.id)
      ? tasks.map(existing => existing.id === task.id ? task : existing)
      : [...tasks, task]);
    this.editingTask.set(null);
  }

  deleteTask(task: Task): void {
    if (!this.project()) return;
    this.service.deleteTask(this.project()!.id, task.id).subscribe({
      next: () => {
        this.tasks.update(tasks => tasks.filter(existing => existing.id !== task.id));
        this.allTasks.update(tasks => tasks.filter(existing => existing.id !== task.id));
      },
      error: error => this.error.set(apiErrorMessage(error)),
    });
  }
}
