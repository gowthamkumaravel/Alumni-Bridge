import { AccountType, UserRole, UserStatus } from '../../core/models/enums';

export interface UserResponse {
  employeeId: string;
  name: string;
  role: UserRole;
  email: string;
  phone?: string;
  accountType: AccountType;
  status: UserStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  employeeId: string;
  name: string;
  role: UserRole;
  email: string;
  phone?: string;
  accountType?: AccountType;
  status?: UserStatus;
  password?: string;
}

export interface UpdateUserRequest {
  name?: string;
  role?: UserRole;
  email?: string;
  phone?: string;
  accountType?: AccountType;
  status?: UserStatus;
}
