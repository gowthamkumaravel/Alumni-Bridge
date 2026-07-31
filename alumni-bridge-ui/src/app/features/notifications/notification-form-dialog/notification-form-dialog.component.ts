import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

import { NOTIFICATION_CATEGORIES } from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import { CreateNotificationRequest } from '../notification.model';
import { NotificationsService } from '../notifications.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-notification-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './notification-form-dialog.component.html',
})
export class NotificationFormDialogComponent {
  readonly categories = NOTIFICATION_CATEGORIES;
  saving = false;

  readonly form = this.fb.group({
    employeeId: ['' as string, [Validators.required]],
    userName: [''],
    userEmail: ['', Validators.email],
    category: ['OFFBOARDING' as string, Validators.required],
    message: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: NotificationsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<
      NotificationFormDialogComponent,
      boolean
    >,
  ) {}

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    const value = this.form.getRawValue();
    const request: CreateNotificationRequest = {
      employeeId: value.employeeId!,
      userName: value.userName || undefined,
      userEmail: value.userEmail || undefined,
      category: value.category as CreateNotificationRequest['category'],
      message: value.message!,
    };

    this.service.create(request).subscribe({
      next: () => {
        this.toast.success('Notification sent.');
        this.dialogRef.close(true);
      },
      error: () => (this.saving = false),
    });
  }
}
