import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import { NotificationResponse } from '../notification.model';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-notification-detail-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>Notification #{{ data.notificationId }}</h2>
    <mat-dialog-content>
      <div class="detail-grid">
        <span class="label">Recipient</span>
        <span>{{ data.userName || '—' }} (Employee {{ data.employeeId }})</span>

        <span class="label">Category</span>
        <span>{{ data.category | enumLabel }}</span>

        <span class="label">Status</span>
        <span>{{ data.status | enumLabel }}</span>

        <span class="label">Created</span>
        <span>{{ data.createdDate | date: 'medium' }}</span>

        <span class="label">Message</span>
        <span>{{ data.message }}</span>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
})
export class NotificationDetailDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: NotificationResponse) {}
}
