import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { BehaviorSubject } from 'rxjs';
import { signal } from '@angular/core';
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
import { TaskDetailComponent } from './components/task-detail.component';
import { App } from './app';
import { routes } from './app.routes';
import { environment } from '../environments/environment';
import { apiErrorMessage } from './shared/api-error';
import { Member, Notification, Project, Task, User } from './models';

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
    const service = TestBed.inject(NotificationService);
    service.list().subscribe();
    http.expectOne(`${environment.apiUrl}/notifications`).flush([]);
    service.markAsRead(4).subscribe();
    http.expectOne({ method: 'PATCH', url: `${environment.apiUrl}/notifications/4/read` }).flush(null);
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

  // Vérifie que le formulaire d’inscription refuse les champs incomplets.
  it('refuse une inscription invalide', () => {
    const register = vi.fn(() => of(user));
    setup({ register });
    const component = TestBed.createComponent(RegisterComponent).componentInstance;
    component.submit();
    expect(register).not.toHaveBeenCalled();
    expect(component.form.touched).toBe(true);
  });

  // Vérifie que le serveur peut refuser l’inscription avec un message lisible.
  it('affiche l’erreur d’inscription', () => {
    const register = vi.fn(() => throwError(() => new HttpErrorResponse({ status: 409 })));
    setup({ register });
    const component = TestBed.createComponent(RegisterComponent).componentInstance;
    component.form.setValue({ username: 'Alice', email: user.email, password: 'password' });
    component.submit();
    expect(component.error).toContain('existe');
  });
});

describe('Composants projets et tâches', () => {
  it('charge et crée un projet, y compris les erreurs', () => {
    const service = { list: vi.fn(() => of([project])), create: vi.fn(() => of({ ...project, id: 2 })) };
    TestBed.configureTestingModule({ imports: [ProjectListComponent], providers: [{ provide: ProjectService, useValue: service }, provideRouter([])] });
    const fixture = TestBed.createComponent(ProjectListComponent);
    const component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.projects()).toHaveLength(1);
    component.form.setValue({ name: 'Nouveau', description: '' });
    component.create();
    expect(component.projects()).toHaveLength(2);
    service.list.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 404 })));
    component.load();
    expect(component.error()).toContain('introuvable');
  });

  // Vérifie que le formulaire de projet invalide ne déclenche aucun appel HTTP.
  it('refuse la création d’un projet sans nom', () => {
    const service = { list: vi.fn(() => of([])), create: vi.fn(() => of(project)) };
    TestBed.configureTestingModule({ imports: [ProjectListComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const component = TestBed.createComponent(ProjectListComponent).componentInstance;
    component.create();
    expect(service.create).not.toHaveBeenCalled();
    expect(component.form.touched).toBe(true);
  });

  // Vérifie que l’erreur de création est affichée et que le chargement est terminé.
  it('gère une erreur lors de la création d’un projet', () => {
    const service = { list: vi.fn(() => of([])), create: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 403 }))) };
    TestBed.configureTestingModule({ imports: [ProjectListComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const component = TestBed.createComponent(ProjectListComponent).componentInstance;
    component.form.setValue({ name: 'Projet', description: '' });
    component.create();
    expect(component.error()).toContain('droits');
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

    const fixture = TestBed.createComponent(ProjectDetailComponent);
    const component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.isAdmin()).toBe(true);
    component.memberForm.setValue({ email: 'bob@example.com', role: 'MEMBER' });
    component.addMember();
    expect(service.addMember).toHaveBeenCalled();
  });

  it('calcule le tableau de bord et change le rôle d’un membre', () => {
    const service = {
      get: vi.fn(() => of(project)), members: vi.fn(() => of([member])), tasks: vi.fn(() => of([task])),
      updateMemberRole: vi.fn(() => of({ ...member, role: 'MEMBER' })),
    };
    TestBed.configureTestingModule({
      imports: [ProjectDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: AuthService, useValue: { currentUser: () => user } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } },
      ],
    });
    const fixture = TestBed.createComponent(ProjectDetailComponent);
    const component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.count('A_FAIRE')).toBe(1);
    component.changeRole(member, { target: { value: 'MEMBER' } } as unknown as Event);
    expect(service.updateMemberRole).toHaveBeenCalledWith(1, 1, 'MEMBER');
  });

  // Vérifie les états invalides et en lecture seule du détail de projet.
  it('refuse un identifiant de projet invalide et distingue un observateur', () => {
    const service = { get: vi.fn(), members: vi.fn(), tasks: vi.fn() };
    TestBed.configureTestingModule({
      imports: [ProjectDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: AuthService, useValue: { currentUser: () => user } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '0' } } } },
      ],
    });
    const fixture = TestBed.createComponent(ProjectDetailComponent);
    const component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.error()).toContain('invalide');
    expect(service.get).not.toHaveBeenCalled();
  });

  // Vérifie les erreurs de chargement des membres et des tâches.
  it('affiche les erreurs du chargement des membres et tâches', () => {
    const error = new HttpErrorResponse({ status: 404 });
    const service = {
      get: vi.fn(() => of(project)), members: vi.fn(() => throwError(() => error)),
      tasks: vi.fn(() => throwError(() => error)),
    };
    TestBed.configureTestingModule({
      imports: [ProjectDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: AuthService, useValue: { currentUser: () => user } },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } },
      ],
    });
    const fixture = TestBed.createComponent(ProjectDetailComponent);
    const component = fixture.componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.error()).toContain('introuvable');
  });

  // Vérifie la mise à jour locale après création, modification et suppression d’une tâche.
  it('rafraîchit les tâches après leurs opérations', () => {
    const service = {
      deleteTask: vi.fn(() => of(void 0)), get: vi.fn(() => of(project)),
      members: vi.fn(() => of([member])), tasks: vi.fn(() => of([task])),
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
    const updated = { ...task, title: 'Tâche modifiée' };
    component.saveTask(updated);
    expect(component.tasks()[0].title).toBe('Tâche modifiée');
    component.deleteTask(updated);
    expect(component.tasks()).toEqual([]);
    expect(service.deleteTask).toHaveBeenCalledWith(1, 2);
  });

  // Vérifie le filtre de statut et le refus d’une action d’ajout invalide.
  it('filtre les tâches et valide le formulaire membre', () => {
    const secondTask = { ...task, id: 3, status: 'FAIT' as const };
    const service = {
      get: vi.fn(() => of(project)), members: vi.fn(() => of([member])),
      tasks: vi.fn(() => of([task, secondTask])), addMember: vi.fn(() => of(member)),
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
    component.filterTasks({ target: { value: 'FAIT' } } as unknown as Event);
    expect(component.tasks().map(item => item.id)).toEqual([3]);
    component.memberForm.reset({ email: '', role: 'MEMBER' });
    component.addMember();
    expect(service.addMember).not.toHaveBeenCalled();
  });

  // Vérifie que les erreurs d’administration restent visibles à l’utilisateur.
  it('affiche les erreurs de modification des membres et de suppression', () => {
    const error = new HttpErrorResponse({ status: 403 });
    const service = {
      get: vi.fn(() => of(project)), members: vi.fn(() => of([member])),
      tasks: vi.fn(() => of([task])), updateMemberRole: vi.fn(() => throwError(() => error)),
      deleteTask: vi.fn(() => throwError(() => error)),
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
    component.changeRole(member, { target: { value: 'VIEWER' } } as unknown as Event);
    expect(component.error()).toContain('droits');
    component.deleteTask(task);
    expect(component.error()).toContain('droits');
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

  // Vérifie qu’une erreur d’enregistrement laisse le formulaire utilisable.
  it('affiche une erreur lors de la sauvegarde d’une tâche', () => {
    const service = { createTask: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 400 }))) };
    TestBed.configureTestingModule({ imports: [TaskFormComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const fixture = TestBed.createComponent(TaskFormComponent);
    const component = fixture.componentInstance;
    component.projectId = 1;
    fixture.detectChanges();
    component.form.controls.title.setValue('Tâche');
    component.submit();
    expect(component.saving()).toBe(false);
    expect(component.error()).toContain('invalide');
  });

  // Vérifie qu’une modification échouée arrête aussi l’indicateur de sauvegarde.
  it('gère une erreur lors de la modification d’une tâche', () => {
    const service = { updateTask: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 404 }))) };
    TestBed.configureTestingModule({ imports: [TaskFormComponent], providers: [{ provide: ProjectService, useValue: service }] });
    const component = TestBed.createComponent(TaskFormComponent).componentInstance;
    component.projectId = 1;
    component.task = task;
    component.form.controls.title.setValue('Tâche');
    component.submit();
    expect(component.saving()).toBe(false);
    expect(component.error()).toContain('introuvable');
  });

  it('charge le détail d’une tâche et son historique', () => {
    const service = {
      getTask: vi.fn(() => of(task)),
      history: vi.fn(() => of([{ id: 1, modifiedField: 'status', oldValue: 'A_FAIRE', newValue: 'FAIT',
        modifiedDate: '2026-01-01', modifiedBy: 'Alice' }])),
    };
    TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: (key: string) => key === 'projectId' ? '1' : '2' } } } },
      ],
    });
    const component = TestBed.createComponent(TaskDetailComponent).componentInstance;
    component.ngOnInit();
    expect(component.task()).toEqual(task);
    expect(component.history()).toHaveLength(1);
  });

  it('arrête le chargement du détail de tâche en cas d’erreur', () => {
    const service = {
      getTask: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 404 }))),
      history: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 404 }))),
    };
    TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '2' } } } },
      ],
    });
    const component = TestBed.createComponent(TaskDetailComponent).componentInstance;
    component.ngOnInit();
    expect(component.loading()).toBe(false);
    expect(component.historyLoading()).toBe(false);
    expect(component.error()).toContain('introuvable');
  });

  it('arrête les deux chargements quand les identifiants sont invalides', () => {
    const service = { getTask: vi.fn(), history: vi.fn() };
    TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        { provide: ProjectService, useValue: service },
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '0' } } } },
      ],
    });
    const component = TestBed.createComponent(TaskDetailComponent).componentInstance;
    component.ngOnInit();
    expect(component.loading()).toBe(false);
    expect(component.historyLoading()).toBe(false);
    expect(service.getTask).not.toHaveBeenCalled();
  });
});

describe('Notifications dans la racine', () => {
  const notification: Notification = {
    id: 5, message: 'Tâche assignée', read: false, createdAt: '', taskId: 5, projectId: 2, userId: 1,
  };

  it('recharge les notifications après connexion et les vide à la déconnexion', async () => {
    const userChanges = new BehaviorSubject<User | null>(null);
    const auth = {
      currentUser: vi.fn(() => null),
      currentUserSignal: signal<User | null>(null),
      userChanges$: userChanges.asObservable(),
      logout: vi.fn(() => userChanges.next(null)),
    };
    const service = { list: vi.fn(() => of([notification])), markAsRead: vi.fn(() => of(void 0)) };
    const router = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: NotificationService, useValue: service },
        provideRouter(routes),
      ],
    });
    const fixture = TestBed.createComponent(App);
    userChanges.next(user);
    await new Promise(resolve => setTimeout(resolve, 10));
    expect(service.list).toHaveBeenCalled();
    expect(fixture.componentInstance.notifications()).toEqual([notification]);
    fixture.componentInstance.logout();
    expect(fixture.componentInstance.notifications()).toEqual([]);
    fixture.destroy();
  });

  it('marque une notification comme lue et recalcule le compteur', () => {
    const userChanges = new BehaviorSubject<User | null>(user);
    const auth = {
      currentUser: vi.fn(() => user),
      currentUserSignal: signal<User | null>(user),
      userChanges$: userChanges.asObservable(),
      logout: vi.fn(),
    };
    const service = { list: vi.fn(() => of([])), markAsRead: vi.fn(() => of(void 0)) };
    const router = { navigate: vi.fn() };
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: NotificationService, useValue: service },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {} },
      ],
    });
    const fixture = TestBed.createComponent(App);
    fixture.componentInstance.notifications.set([{ ...notification }]);
    expect(fixture.componentInstance.unreadCount()).toBe(1);
    fixture.componentInstance.markNotificationAsRead(fixture.componentInstance.notifications()[0]);
    expect(service.markAsRead).toHaveBeenCalledWith(notification.id);
    expect(fixture.componentInstance.unreadCount()).toBe(0);
    expect(router.navigate).toHaveBeenCalledWith(['/projects', 2, 'tasks', 5]);
    fixture.destroy();
  });

  it('navigue pas si le marquage de notification échoue', () => {
    const auth = {
      currentUser: vi.fn(() => user),
      currentUserSignal: signal<User | null>(user),
      userChanges$: of(user),
      logout: vi.fn(),
    };
    const router = { navigate: vi.fn() };
    const service = {
      list: vi.fn(() => of([])),
      markAsRead: vi.fn(() => throwError(() => new HttpErrorResponse({ status: 403 }))),
    };
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: NotificationService, useValue: service },
        { provide: Router, useValue: router },
        { provide: ActivatedRoute, useValue: {} },
      ],
    });
    const fixture = TestBed.createComponent(App);
    fixture.componentInstance.notifications.set([{ ...notification }]);
    fixture.componentInstance.markNotificationAsRead(fixture.componentInstance.notifications()[0]);
    expect(router.navigate).not.toHaveBeenCalled();
    expect(fixture.componentInstance.notificationError()).toContain('droits');
    fixture.destroy();
  });
});
