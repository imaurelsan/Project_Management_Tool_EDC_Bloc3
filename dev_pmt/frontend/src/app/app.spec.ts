import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { App } from './app';
import { AuthService } from './services/auth.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient()],
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render the navigation', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.brand')?.textContent).toContain('PMT');
  });

  it('déconnecte l’utilisateur et revient à la connexion', () => {
    const logout = vi.fn();
    const navigate = vi.fn();
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: AuthService, useValue: { currentUser: () => null, logout } },
      ],
    });
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockImplementation(navigate);
    const app = TestBed.createComponent(App).componentInstance;
    app.logout();
    expect(logout).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/login']);
  });
});
