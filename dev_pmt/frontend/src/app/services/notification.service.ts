import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Notification } from '../models';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  list(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${environment.apiUrl}/notifications`);
  }
}
