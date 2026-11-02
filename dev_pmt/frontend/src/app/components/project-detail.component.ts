import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { ProjectService } from '../services/project.service';
import { AuthService } from '../services/auth.service';
import { Member, Project, ProjectRole, Task } from '../models';
import { apiErrorMessage } from '../shared/api-error';
import { TaskFormComponent } from './task-form.component';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TaskFormComponent],
  template: `
    @if (project) {
      <section class="page">
        <a routerLink="/projects">← Mes projets</a><h1>{{ project.name }}</h1><p>{{ project.description }}</p>
        @if (error) { <p class="error">{{ error }}</p> }
        <h2>Membres</h2>
        <ul>@for (member of members; track member.userId) { <li>{{ member.username }} — {{ member.role }}</li> }</ul>
        @if (isAdmin) {
          <form class="card form-grid" [formGroup]="memberForm" (ngSubmit)="addMember()">
            <input placeholder="E-mail du membre" formControlName="email">
            <select formControlName="role"><option value="MEMBER">Membre</option><option value="VIEWER">Observateur</option><option value="ADMINISTRATOR">Administrateur</option></select>
            <button type="submit">Ajouter</button>
          </form>
        }
        <h2>Tâches</h2>
        <app-task-form [projectId]="project.id" (saved)="tasks = [...tasks, $event]"></app-task-form>
        <div class="grid">@for (task of tasks; track task.id) { <article class="card"><strong>{{ task.title }}</strong><span>{{ task.status }} · {{ task.priority }}</span></article> } @empty { <p>Aucune tâche.</p> }</div>
      </section>
    }
  `,
})
export class ProjectDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(ProjectService);
  private readonly auth = inject(AuthService);
  readonly memberForm = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]], role: ['MEMBER' as ProjectRole, Validators.required],
  });
  project: Project | null = null;
  members: Member[] = [];
  tasks: Task[] = [];
  error = '';
  isAdmin = false;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.get(id).subscribe({ next: project => this.project = project, error: e => this.error = apiErrorMessage(e) });
    this.service.members(id).subscribe({ next: members => {
      this.members = members;
      this.isAdmin = members.some(m => m.userId === this.auth.currentUser()?.id && m.role === 'ADMINISTRATOR');
    }, error: e => this.error = apiErrorMessage(e) });
    this.service.tasks(id).subscribe({ next: tasks => this.tasks = tasks, error: e => this.error = apiErrorMessage(e) });
  }

  addMember(): void {
    if (this.memberForm.invalid || !this.project) { this.memberForm.markAllAsTouched(); return; }
    const value = this.memberForm.getRawValue();
    this.service.addMember(this.project.id, value.email, value.role).subscribe({
      next: member => { this.members = [...this.members, member]; this.memberForm.reset({ email: '', role: 'MEMBER' }); },
      error: e => this.error = apiErrorMessage(e),
    });
  }
}
