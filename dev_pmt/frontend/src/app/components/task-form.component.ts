import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { ProjectService } from '../services/project.service';
import { Task, TaskPriority, TaskRequest, TaskStatus } from '../models';
import { apiErrorMessage } from '../shared/api-error';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule],
  selector: 'app-task-form',
  template: `
    <form class="card form-grid" [formGroup]="form" (ngSubmit)="submit()">
      <input placeholder="Titre" formControlName="title">
      <textarea placeholder="Description" formControlName="description"></textarea>
      <select formControlName="status">@for (status of statuses; track status) { <option [value]="status">{{ status }}</option> }</select>
      <select formControlName="priority">@for (priority of priorities; track priority) { <option [value]="priority">{{ priority }}</option> }</select>
      <input type="date" formControlName="dueDate">
      <input type="number" placeholder="ID de l'assigné" formControlName="assigneeId">
      @if (form.invalid && form.touched) { <small>Le titre, le statut et la priorité sont obligatoires.</small> }
      @if (error) { <p class="error">{{ error }}</p> }
      <button type="submit">{{ task ? 'Modifier' : 'Créer' }}</button>
    </form>
  `,
})
export class TaskFormComponent {
  @Input({ required: true }) projectId!: number;
  @Input() task: Task | null = null;
  @Output() saved = new EventEmitter<Task>();
  private readonly service = inject(ProjectService);
  readonly statuses: TaskStatus[] = ['A_FAIRE', 'EN_COURS', 'FAIT', 'ANNULE'];
  readonly priorities: TaskPriority[] = ['FAIBLE', 'MOYEN', 'HAUTE', 'CRITIQUE'];
  readonly form = inject(FormBuilder).nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', Validators.maxLength(2000)],
    status: ['A_FAIRE' as TaskStatus, Validators.required],
    priority: ['MOYEN' as TaskPriority, Validators.required],
    dueDate: [''],
    assigneeId: [null as number | null],
  });
  error = '';

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const request = this.form.getRawValue() as TaskRequest;
    const operation = this.task
      ? this.service.updateTask(this.projectId, this.task.id, request)
      : this.service.createTask(this.projectId, request);
    operation.subscribe({ next: task => this.saved.emit(task), error: e => this.error = apiErrorMessage(e) });
  }
}
