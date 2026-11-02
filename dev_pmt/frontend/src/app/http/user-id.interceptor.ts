import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const user = inject(AuthService).currentUser();
  return next(user ? req.clone({ setHeaders: { 'X-User-Id': String(user.id) } }) : req);
};
