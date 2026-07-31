import { AccountType, UserRole, UserStatus } from './enums';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  employeeId: string;
  name: string;
  role?: UserRole;
  email: string;
  phone?: string;
  password: string;
  accountType?: AccountType;
  status?: UserStatus;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  employeeId: string;
  name: string;
  email: string;
  role: UserRole;
  accountType: AccountType;
  status: UserStatus;
}

export interface CurrentUser {
  employeeId: string;
  name: string;
  email: string;
  phone?: string;
  role: UserRole;
  accountType: AccountType;
  status: UserStatus;
}
