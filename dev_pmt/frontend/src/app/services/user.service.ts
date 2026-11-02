import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { User } from '../models';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  register(request: { username: string; email: string; password: string }): Observable<User> {
    return this.http.post<User>(`${environment.apiUrl}/auth/register`, request);
  }
}
