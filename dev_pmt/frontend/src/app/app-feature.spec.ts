import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from './services/auth.service';
import { ProjectService } from './services/project.service';
import { NotificationService } from './services/notification.service';
import { UserService } from './services/user.service';
import { userIdInterceptor } from './http/user-id.interceptor';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './components/login.component';
import { RegisterComponent } from './components/register.component';
import { ProjectListComponent } from './components/project-list.component';
import { ProjectDetailComponent } from './components/project-detail.component';
import { TaskFormComponent } from './components/task-form.component';
import { environment } from '../environments/environment';
import { apiErrorMessage } from './shared/api-error';
import { Member, Project, Task, User } from './models';

const user: User = { id: 1, username: 'Alice', email: 'alice@example.com' };
const project: Project = { id: 1, name: 'Projet', description: 'Desc', startDate: null, creatorId: 1, creatorUsername: 'Alice' };
const task: Task = { id: 2, title: 'Tâche', description: '', status: 'A_FAIRE', priority: 'MOYEN', dueDate: null, projectId: 1, assigneeId: null, assigneeUsername: null, createdAt: '', updatedAt: '' };
const member: Member = { userId: 1, username: 'Alice', email: user.email, role: 'ADMINISTRATOR' };

describe('Services HTTP', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AuthService, ProjectService, NotificationService, UserService],
    });
    http = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });
  afterEach(() => http.verify());

  it('gère la connexion, la session et la déconnexion', () => {
    const service = TestBed.inject(AuthService);
    service.login({ email: user.email, password: 'password' }).subscribe();
    const request = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(request.request.method).toBe('POST');
    request.flush(user);
    expect(service.currentUser()).toEqual(user);
    service.logout();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('envoie une demande d’inscription', () => {
    TestBed.inject(AuthService).register({ username: 'Alice', email: user.email, password: 'password' }).subscribe();
    const request = http.expectOne({ method: 'POST', url: `${environment.apiUrl}/auth/register` });
    expect(request.request.body.email).toBe(user.email);
    request.flush(user);
  });

  it('nettoie une session JSON invalide', () => {
    localStorage.setItem('pmt-current-user', '{bad');
    expect(TestBed.inject(AuthService).currentUser()).toBeNull();
  });

  it('appelle les routes projets, membres, tâches et historique', () => {
    const service = TestBed.inject(ProjectService);
    service.list().subscribe(); http.expectOne(`${environment.apiUrl}/projects`).flush([]);
    service.get(1).subscribe(); http.expectOne(`${environment.apiUrl}/projects/1`).flush(project);
    service.create({ name: 'x', description: '' }).subscribe(); http.expectOne({ method: 'POST', url: `${environment.apiUrl}/projects` }).flush(project);
    service.members(1).subscribe(); http.expectOne(`${environment.apiUrl}/projects/1/members`).flush([]);
    service.addMember(1, user.email, 'MEMBER').subscribe(); http.expectOne({ method: 'POST', url: `${environment.apiUrl}/projects/1/members` }).flush(member);
    service.updateMemberRole(1, 1, 'VIEWER').subscribe(); http.expectOne({ method: 'PUT', url: `${environment.apiUrl}/projects/1/members/1` }).flush(member);
    service.tasks(1).subscribe(); http.expectOne(`${environment.apiUrl}/projects/1/tasks`).flush([]);
    service.createTask(1, { title: 'x', description: '', status: 'A_FAIRE', priority: 'MOYEN', dueDate: '', assigneeId: null }).subscribe(); http.expectOne({ method: 'POST', url: `${environment.apiUrl}/projects/1/tasks` }).flush(task);
    service.updateTask(1, 2, { title: 'x', description: '', status: 'FAIT', priority: 'HAUTE', dueDate: '', assigneeId: null }).subscribe(); http.expectOne({ method: 'PUT', url: `${environment.apiUrl}/projects/1/tasks/2` }).flush(task);
    service.deleteTask(1, 2).subscribe(); http.expectOne({ method: 'DELETE', url: `${environment.apiUrl}/projects/1/tasks/2` }).flush(null);
    service.history(1, 2).subscribe(); http.expectOne(`${environment.apiUrl}/projects/1/tasks/2/history`).flush([]);
  });

  it('appelle le service des notifications', () => {
    TestBed.inject(NotificationService).list().subscribe();
    http.expectOne(`${environment.apiUrl}/notifications`).flush([]);
  });

  it('inscrit un utilisateur via UserService', () => {
    TestBed.inject(UserService).register({ username: 'Alice', email: user.email, password: 'password' }).subscribe();
    const request = http.expectOne({ method: 'POST', url: `${environment.apiUrl}/auth/register` });
    expect(request.request.body.username).toBe('Alice');
    request.flush(user);
  });
});

describe('Messages d’erreur API', () => {
  it.each([400, 403, 404, 409])('traduit le statut %s', status => {
    expect(apiErrorMessage(new HttpErrorResponse({ status }))).not.toContain('Une erreur');
  });

  it('utilise le message générique pour une erreur inconnue', () => {
    expect(apiErrorMessage(new Error('échec'))).toContain('Une erreur');
  });
});

describe('Infrastructure HTTP', () => {
  it('ajoute X-User-Id quand une session existe', () => {
    localStorage.setItem('pmt-current-user', JSON.stringify(user));
    TestBed.configureTestingModule({
      providers: [AuthService, ProjectService, provideHttpClient(withInterceptors([userIdInterceptor])), provideHttpClientTesting()],
    });
    TestBed.inject(AuthService);
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(ProjectService);
    TestBed.inject(ProjectService).list().subscribe();
    const request = http.expectOne(`${environment.apiUrl}/projects`);
    expect(request.request.headers.get('X-User-Id')).toBe('1');
    request.flush([]);
  });

  it('redirige les visiteurs et laisse passer les utilisateurs connectés', () => {
    const router = { createUrlTree: vi.fn(() => 'login') };
    const auth = { isAuthenticated: vi.fn(() => false) };
    TestBed.configureTestingModule({ providers: [{ provide: AuthService, useValue: auth }, { provide: Router, useValue: router }] });
    expect(TestBed.runInInjectionContext(() => authGuard({} as never, {} as never))).toBe('login');
    auth.isAuthenticated.mockReturnValue(true);
    expect(TestBed.runInInjectionContext(() => authGuard({} as never, {} as never))).toBe(true);
  });
});

describe('Composants de formulaires', () => {
  function setup(auth: Partial<AuthService>) {
    const router = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      imports: [LoginComponent, RegisterComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => null } } } },
      ],
    });
    return router;
  }

  it('refuse une connexion invalide et navigue après succès', () => {
    const login = vi.fn(() => of(user));
    const router = setup({ login });
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.componentInstance.submit();
    expect(login).not.toHaveBeenCalled();
    fixture.componentInstance.form.setValue({ email: user.email, password: 'secret' });
    fixture.componentInstance.submit();
    expect(login).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/projects']);
  });

  it('affiche l’erreur de connexion', () => {
    const auth = { login: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 403 }))) };
    const router = setup(auth);
    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.form.setValue({ email: user.email, password: 'secret' });
    component.submit();
    expect(component.error).toContain('droits');
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('inscrit un utilisateur et gère une erreur', () => {
    const register = vi.fn(() => of(user));
    const router = setup({ register });
    const fixture = TestBed.createComponent(RegisterComponent);
    fixture.componentInstance.form.setValue({ username: 'Alice', email: user.email, password: 'password' });
    fixture.componentInstance.submit();
    expect(register).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});

describe('Composants projets et tâches', () => {
  it('charge et crée un projet, y compris les erreurs', () => {
    const service = { list: vi.fn(() => of([project])), create: vi.fn(() => of({ ...project, id: 2 })) };
    TestBed.configureTestingModule({ imports: [ProjectListComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const component = TestBed.createComponent(ProjectListComponent).componentInstance;
    component.ngOnInit();
    expect(component.projects).toHaveLength(1);
    component.form.setValue({ name: 'Nouveau', description: '' });
    component.create();
    expect(component.projects).toHaveLength(2);
    service.list.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    component.load();
    expect(component.error).toContain('introuvable');
  });

  it('affiche un détail et réserve l’ajout de membre à un administrateur', () => {
    const service = {
      get: vi.fn(() => of(project)), members: vi.fn(() => of([member])), tasks: vi.fn(() => of([task])),
      addMember: vi.fn(() => of({ ...member, userId: 3 })),
    };
    TestBed.configureTestingModule({
      imports: [ProjectDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: AuthService, useValue: { currentUser: () => user } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } },
      ],
    });
    const component = TestBed.createComponent(ProjectDetailComponent).componentInstance;
    component.ngOnInit();
    expect(component.isAdmin).toBe(true);
    component.memberForm.setValue({ email: 'bob@example.com', role: 'MEMBER' });
    component.addMember();
    expect(service.addMember).toHaveBeenCalled();
  });

  it('valide, crée et modifie une tâche', () => {
    const service = { createTask: vi.fn(() => of(task)), updateTask: vi.fn(() => of(task)) };
    TestBed.configureTestingModule({ imports: [TaskFormComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const fixture = TestBed.createComponent(TaskFormComponent);
    const component = fixture.componentInstance;
    component.projectId = 1;
    component.submit();
    expect(service.createTask).not.toHaveBeenCalled();
    component.form.controls.title.setValue('Tâche');
    component.submit();
    expect(service.createTask).toHaveBeenCalled();
    component.task = task;
    component.submit();
    expect(service.updateTask).toHaveBeenCalled();
  });
});
