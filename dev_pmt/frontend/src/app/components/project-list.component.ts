import { Component, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProjectService } from '../services/project.service';
import { Project } from '../models';
import { apiErrorMessage } from '../shared/api-error';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="page">
      <div class="page-header"><h1>Mes projets</h1></div>
      <form class="card form-grid" [formGroup]="form" (ngSubmit)="create()">
        <input placeholder="Nom du projet" formControlName="name">
        <input placeholder="Description" formControlName="description">
        <button type="submit">Créer</button>
      </form>
      @if (loading()) { <p>Chargement des projets...</p> }
      @if (error()) { <p class="error">{{ error() }}</p> }
      <div class="grid">
        @for (project of projects(); track project.id) {
          <a class="card project" [routerLink]="['/projects', project.id]">
            <h2>{{ project.name }}</h2><p>{{ project.description }}</p>
          </a>
        } @empty { <p>Aucun projet pour le moment.</p> }
      </div>
    </section>
  `,
})
export class ProjectListComponent implements OnInit {
  private readonly service = inject(ProjectService);
  readonly form = inject(FormBuilder).nonNullable.group({
    name: ['', Validators.required], description: [''],
  });
  projects = signal<Project[]>([]);
  loading = signal(false);
  error = signal('');

  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading.set(true);
    this.service.list().subscribe({
      next: projects => { this.projects.set(projects); this.loading.set(false); },
      error: e => { this.error.set(apiErrorMessage(e)); this.loading.set(false); },
    });
  }
  create(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.service.create(this.form.getRawValue()).subscribe({
      next: project => { this.projects.update(projects => [...projects, project]); this.form.reset(); },
      error: e => this.error.set(apiErrorMessage(e)),
    });
  }
}
