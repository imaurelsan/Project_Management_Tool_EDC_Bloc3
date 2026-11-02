import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { apiErrorMessage } from '../shared/api-error';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="card narrow">
      <h1>Inscription</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>Nom d'utilisateur <input formControlName="username"></label>
        <label>E-mail <input type="email" formControlName="email"></label>
        <label>Mot de passe <input type="password" formControlName="password"></label>
        @if (form.invalid && form.touched) { <small>Complétez correctement le formulaire.</small> }
        @if (error) { <p class="error">{{ error }}</p> }
        <button type="submit">S'inscrire</button>
      </form>
      <a routerLink="/login">J'ai déjà un compte</a>
    </section>
  `,
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });
  error = '';

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.error = '';
    this.auth.register(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/login']),
      error: error => this.error = apiErrorMessage(error),
    });
  }
}
