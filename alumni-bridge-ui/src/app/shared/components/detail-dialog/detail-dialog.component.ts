import { CommonModule } from '@angular/common';
import { Component, Inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';

export interface DetailRow {
  label: string;
  value: string | number | null | undefined;
}

export interface DetailDialogData {
  title: string;
  rows: DetailRow[];
}

/** Generic read-only key/value viewer reused by feature "View details" actions. */
@Component({
  selector: 'app-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.title }}</h2>
    <mat-dialog-content>
      <div class="detail-grid">
        <ng-container *ngFor="let row of data.rows">
          <span class="label">{{ row.label }}</span>
          <span>{{ row.value === null || row.value === undefined || row.value === '' ? '—' : row.value }}</span>
        </ng-container>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
})
export class DetailDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: DetailDialogData) {}
}
