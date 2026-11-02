import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./components/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./components/register.component').then(m => m.RegisterComponent) },
  { path: 'projects', canActivate: [authGuard], loadComponent: () => import('./components/project-list.component').then(m => m.ProjectListComponent) },
  { path: 'projects/:id', canActivate: [authGuard], loadComponent: () => import('./components/project-detail.component').then(m => m.ProjectDetailComponent) },
  { path: 'projects/:projectId/tasks/:taskId', canActivate: [authGuard], loadComponent: () => import('./components/task-detail.component').then(m => m.TaskDetailComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'projects' },
  { path: '**', redirectTo: 'projects' },
];
