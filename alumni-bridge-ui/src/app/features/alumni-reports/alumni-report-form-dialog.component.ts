import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

import { REPORT_METRICS, REPORT_SCOPES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import { CreateAlumniReportRequest } from './alumni-report.model';
import { AlumniReportsService } from './alumni-reports.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-alumni-report-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>Generate Report</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="reportForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline">
          <mat-label>Scope</mat-label>
          <mat-select formControlName="scope">
            <mat-option *ngFor="let s of scopes" [value]="s">{{ s | enumLabel }}</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Metric</mat-label>
          <mat-select formControlName="metric">
            <mat-option *ngFor="let m of metrics" [value]="m">{{ m | enumLabel }}</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Metric Value</mat-label>
          <input matInput formControlName="metricValue" />
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="reportForm" [disabled]="saving">Generate</button>
    </mat-dialog-actions>
  `,
})
export class AlumniReportFormDialogComponent {
  readonly scopes = REPORT_SCOPES;
  readonly metrics = REPORT_METRICS;
  saving = false;

  readonly form = this.fb.group({
    scope: ['DEPARTMENT' as string, Validators.required],
    metric: ['ATTRITION_RATE' as string, Validators.required],
    metricValue: [''],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: AlumniReportsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<AlumniReportFormDialogComponent, boolean>,
  ) {}

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const v = this.form.getRawValue();
    const request: CreateAlumniReportRequest = {
      scope: v.scope as CreateAlumniReportRequest['scope'],
      metric: v.metric as CreateAlumniReportRequest['metric'],
      metricValue: v.metricValue || undefined,
    };
    this.service.create(request).subscribe({
      next: () => {
        this.toast.success('Report generated.');
        this.dialogRef.close(true);
      },
      error: () => (this.saving = false),
    });
  }
}
