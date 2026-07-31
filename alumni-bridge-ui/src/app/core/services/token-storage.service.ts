import { Injectable } from '@angular/core';

import { AuthResponse } from '../models/auth.models';

const TOKEN_KEY = 'ab_token';
const USER_KEY = 'ab_user';

/**
 * Thin wrapper around localStorage so the rest of the app never touches
 * storage keys directly. Kept separate from AuthService to avoid a circular
 * dependency with the HTTP interceptor.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  saveToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  saveUser(user: AuthResponse): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  getUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthResponse) : null;
  }

  clear(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }
}
