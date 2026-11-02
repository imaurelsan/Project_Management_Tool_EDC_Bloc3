import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Member, Project, ProjectRequest, ProjectRole, Task, TaskRequest, History } from '../models';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/projects`;

  list(): Observable<Project[]> { return this.http.get<Project[]>(this.baseUrl); }
  get(id: number): Observable<Project> { return this.http.get<Project>(`${this.baseUrl}/${id}`); }
  create(request: ProjectRequest): Observable<Project> { return this.http.post<Project>(this.baseUrl, request); }
  members(projectId: number): Observable<Member[]> { return this.http.get<Member[]>(`${this.baseUrl}/${projectId}/members`); }
  addMember(projectId: number, email: string, role: ProjectRole): Observable<Member> {
    return this.http.post<Member>(`${this.baseUrl}/${projectId}/members`, { email, role });
  }
  updateMemberRole(projectId: number, userId: number, role: ProjectRole): Observable<Member> {
    return this.http.put<Member>(`${this.baseUrl}/${projectId}/members/${userId}`, { role });
  }
  tasks(projectId: number): Observable<Task[]> { return this.http.get<Task[]>(`${this.baseUrl}/${projectId}/tasks`); }
  createTask(projectId: number, request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(`${this.baseUrl}/${projectId}/tasks`, request);
  }
  updateTask(projectId: number, taskId: number, request: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${projectId}/tasks/${taskId}`, request);
  }
  deleteTask(projectId: number, taskId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${projectId}/tasks/${taskId}`);
  }
  history(projectId: number, taskId: number): Observable<History[]> {
    return this.http.get<History[]>(`${this.baseUrl}/${projectId}/tasks/${taskId}/history`);
  }
}
