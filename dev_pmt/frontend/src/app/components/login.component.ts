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
      <h1>Connexion</h1>
      <form [formGroup]="form" (ngSubmit)="submit()">
        <label>E-mail <input type="email" formControlName="email"></label>
        @if (form.controls.email.invalid && form.controls.email.touched) { <small>E-mail invalide.</small> }
        <label>Mot de passe <input type="password" formControlName="password"></label>
        @if (form.controls.password.invalid && form.controls.password.touched) { <small>Mot de passe obligatoire.</small> }
        @if (error) { <p class="error">{{ error }}</p> }
        <button type="submit">Se connecter</button>
      </form>
      <a routerLink="/register">Créer un compte</a>
    </section>
  `,
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly form = inject(FormBuilder).nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });
  error = '';

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.error = '';
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/projects']),
      error: error => this.error = apiErrorMessage(error),
    });
  }
}
