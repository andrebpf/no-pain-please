import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '@angular/fire/auth';
import { from, switchMap } from 'rxjs';

export const firebaseTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(Auth);
  const user = auth.currentUser;
  if (!user) return next(request);

  return from(user.getIdToken()).pipe(
    switchMap(token => next(request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })))
  );
};
