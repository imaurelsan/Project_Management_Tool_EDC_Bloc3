import { Component, DestroyRef, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { EMPTY, Subscription, catchError, switchMap, timer } from 'rxjs';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';
import { Notification, User } from './models';
import { apiErrorMessage } from './shared/api-error';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);
  private pollingSubscription?: Subscription;
  user = this.auth.currentUserSignal;
  notifications = signal<Notification[]>([]);
  notificationsOpen = signal(false);
  notificationError = signal('');
  loadingNotifications = signal(false);

  constructor() {
    const userSubscription = this.auth.userChanges$.subscribe(user => {
      this.notifications.set([]);
      this.notificationsOpen.set(false);
      this.notificationError.set('');
      this.stopNotificationPolling();
      if (user) this.startNotificationPolling();
    });
    this.destroyRef.onDestroy(() => {
      userSubscription.unsubscribe();
      this.stopNotificationPolling();
    });
  }

  private startNotificationPolling(): void {
    this.pollingSubscription = timer(0, 30000).pipe(
      switchMap(() => {
        this.loadingNotifications.set(true);
        return this.notificationService.list().pipe(
          catchError(error => {
            this.notificationError.set(apiErrorMessage(error));
            this.loadingNotifications.set(false);
            return EMPTY;
          }),
        );
      }),
    ).subscribe(notifications => {
      this.notifications.set(notifications);
      this.loadingNotifications.set(false);
    });
  }

  private stopNotificationPolling(): void {
    this.pollingSubscription?.unsubscribe();
    this.pollingSubscription = undefined;
    this.loadingNotifications.set(false);
  }

  toggleNotifications(): void {
    this.notificationsOpen.update(open => !open);
    if (this.notificationsOpen()) {
      this.loadingNotifications.set(true);
      this.notificationService.list().subscribe({
        next: notifications => {
          this.notifications.set(notifications);
          this.loadingNotifications.set(false);
        },
        error: error => {
          this.notificationError.set(apiErrorMessage(error));
          this.loadingNotifications.set(false);
        },
      });
    }
  }

  markNotificationAsRead(notification: Notification): void {
    if (notification.read) return;
    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        this.notifications.update(notifications =>
          notifications.map(item => item.id === notification.id ? { ...item, read: true } : item));
        this.notificationsOpen.set(false);
        this.router.navigate(['/projects', notification.projectId, 'tasks', notification.taskId]);
      },
      error: error => {
        this.notificationError.set(apiErrorMessage(error));
      },
    });
  }

  unreadCount(): number {
    return this.notifications().filter(notification => !notification.read).length;
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
