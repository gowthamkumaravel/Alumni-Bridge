import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import {
  ACCOUNT_TYPES,
  USER_ROLES,
  USER_STATUSES,
} from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import {
  CreateUserRequest,
  UpdateUserRequest,
  UserResponse,
} from '../user.model';
import { UsersService } from '../users.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-user-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './user-form-dialog.component.html',
})
export class UserFormDialogComponent {
  readonly roles = USER_ROLES;
  readonly accountTypes = ACCOUNT_TYPES;
  readonly statuses = USER_STATUSES;

  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    employeeId: ['', Validators.required],
    name: ['', Validators.required],
    role: ['EMPLOYEE' as string, Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    accountType: ['ACTIVE' as string],
    status: ['ACTIVE' as string],
    password: [''],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly usersService: UsersService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<UserFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: UserResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue(data);
      // Employee ID is immutable once created.
      this.form.get('employeeId')?.disable();
    }
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const value = this.form.getRawValue();

    if (this.isEdit && this.data) {
      const request: UpdateUserRequest = {
        name: value.name!,
        role: value.role as UpdateUserRequest['role'],
        email: value.email!,
        phone: value.phone || undefined,
        accountType: value.accountType as UpdateUserRequest['accountType'],
        status: value.status as UpdateUserRequest['status'],
      };
      this.usersService.update(this.data.employeeId, request).subscribe({
        next: () => {
          this.toast.success('User updated successfully.');
          this.dialogRef.close(true);
        },
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateUserRequest = {
        employeeId: value.employeeId!,
        name: value.name!,
        role: value.role as CreateUserRequest['role'],
        email: value.email!,
        phone: value.phone || undefined,
        accountType: value.accountType as CreateUserRequest['accountType'],
        status: value.status as CreateUserRequest['status'],
        password: value.password || undefined,
      };
      this.usersService.create(request).subscribe({
        next: () => {
          this.toast.success('User created successfully.');
          this.dialogRef.close(true);
        },
        error: () => (this.saving = false),
      });
    }
  }
}
