import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storageKey = 'pmt-current-user';

  register(request: { username: string; email: string; password: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/register`, request);
  }

  login(request: { email: string; password: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap(user => localStorage.setItem(this.storageKey, JSON.stringify(user))),
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }

  currentUser(): User | null {
    const stored = localStorage.getItem(this.storageKey);
    if (!stored) return null;
    try {
      return JSON.parse(stored) as User;
    } catch {
      this.logout();
      return null;
    }
  }

  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }
}
