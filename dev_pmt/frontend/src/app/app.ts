import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth.service';
import { User } from './models';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  user: User | null = this.auth.currentUser();
  logout(): void { this.auth.logout(); this.user = null; this.router.navigate(['/login']); }
}
