import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Auth } from '@angular/fire/auth';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { environment } from '../environments/environment';

export const firebaseTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(Auth);
  const user = auth.currentUser;
  if (!user || !targetsApi(request.url)) return next(request);

  const authenticated = (token: string) => request.clone({
    setHeaders: { Authorization: `Bearer ${token}` }
  });

  return from(user.getIdToken()).pipe(
    switchMap(token => next(authenticated(token))),
    catchError(error => {
      if (error.status !== 401) return throwError(() => error);
      return from(user.getIdToken(true)).pipe(
        switchMap(token => next(authenticated(token)))
      );
    })
  );
};

function targetsApi(requestUrl: string): boolean {
  const apiUrl = new URL(environment.apiBaseUrl);
  const targetUrl = new URL(requestUrl, window.location.origin);
  const apiPath = apiUrl.pathname.replace(/\/$/, '');

  return targetUrl.origin === apiUrl.origin
    && (targetUrl.pathname === apiPath || targetUrl.pathname.startsWith(`${apiPath}/`));
}
