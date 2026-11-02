import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storageKey = 'pmt-current-user';
  private readonly userSubject = new BehaviorSubject<User | null>(this.readUser());
  readonly currentUserSignal = signal<User | null>(this.readUser());
  readonly userChanges$ = this.userSubject.asObservable();

  register(request: { username: string; email: string; password: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/register`, request);
  }

  login(request: { email: string; password: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap(user => {
        localStorage.setItem(this.storageKey, JSON.stringify(user));
        this.userSubject.next(user);
        this.currentUserSignal.set(user);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.userSubject.next(null);
    this.currentUserSignal.set(null);
  }

  currentUser(): User | null {
    return this.userSubject.value;
  }

  private readUser(): User | null {
    const stored = localStorage.getItem(this.storageKey);
    if (!stored) return null;
    try {
      return JSON.parse(stored) as User;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }
}
