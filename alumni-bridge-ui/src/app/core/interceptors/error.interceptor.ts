import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/**
 * Central HTTP error handling:
 *  - 401 -> session expired, force logout
 *  - otherwise surface the backend message (ApiErrorResponse.message) as a toast
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const toast = inject(ToastService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const isLoginCall = request.url.endsWith('/auth/login');

      if (error.status === 401 && !isLoginCall) {
        toast.error('Your session has expired. Please sign in again.');
        auth.logout();
      } else if (error.status === 0) {
        toast.error(
          'Unable to reach the server. Check that the API gateway is running.',
        );
      } else {
        toast.error(resolveMessage(error));
      }

      return throwError(() => error);
    }),
  );
};

function resolveMessage(error: HttpErrorResponse): string {
  const body = error.error;
  if (body?.validationErrors) {
    const first = Object.values(body.validationErrors)[0];
    if (typeof first === 'string') {
      return first;
    }
  }
  if (body?.message) {
    return body.message as string;
  }
  return error.statusText || 'Something went wrong. Please try again.';
}
