import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { UserRole } from '../models/enums';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

/**
 * Restricts a route to the roles declared in its `data.roles` array.
 * Usage: { path: 'users', canActivate: [authGuard, roleGuard], data: { roles: ['ADMIN', 'HRBP'] } }
 */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  const allowed = (route.data['roles'] as UserRole[] | undefined) ?? [];

  if (allowed.length === 0 || auth.hasAnyRole(allowed)) {
    return true;
  }

  toast.error('You do not have permission to access that page.');
  return router.createUrlTree(['/dashboard']);
};
