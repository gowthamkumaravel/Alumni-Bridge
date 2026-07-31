import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { BOOMERANG_APPLICATION_STATUSES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import {
  BoomerangApplicationResponse,
  CreateBoomerangApplicationRequest,
  UpdateBoomerangApplicationRequest,
} from './boomerang-application.model';
import { BoomerangApplicationsService } from './boomerang-applications.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-boomerang-application-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Application' : 'New Boomerang Application' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="boomForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline" *ngIf="!isEdit">
          <mat-label>Alumni ID</mat-label>
          <input matInput type="number" formControlName="alumniId" />
          <mat-error>Alumni ID is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Job Requisition ID</mat-label>
          <input matInput formControlName="jobRequisitionId" />
          <mat-error>Job requisition is required</mat-error>
        </mat-form-field>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Previous Tenure (years)</mat-label>
            <input matInput type="number" step="0.1" formControlName="previousTenureYears" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Recruiter (User ID)</mat-label>
            <input matInput type="number" formControlName="recruiterId" />
            <mat-error>Recruiter is required</mat-error>
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline" *ngIf="isEdit">
          <mat-label>Status</mat-label>
          <mat-select formControlName="status">
            <mat-option *ngFor="let s of statuses" [value]="s">{{ s | enumLabel }}</mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="boomForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class BoomerangApplicationFormDialogComponent {
  readonly statuses = BOOMERANG_APPLICATION_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    alumniId: [null as number | null, [Validators.required, Validators.min(1)]],
    jobRequisitionId: ['', Validators.required],
    previousTenureYears: [null as number | null],
    recruiterId: [null as number | null, [Validators.required, Validators.min(1)]],
    status: ['APPLIED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: BoomerangApplicationsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<BoomerangApplicationFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: BoomerangApplicationResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        jobRequisitionId: data.jobRequisitionId,
        previousTenureYears: data.previousTenureYears ?? null,
        recruiterId: data.recruiterId,
        status: data.status,
      });
      this.form.get('alumniId')?.clearValidators();
      this.form.get('alumniId')?.updateValueAndValidity();
    }
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const v = this.form.getRawValue();

    if (this.isEdit && this.data) {
      const request: UpdateBoomerangApplicationRequest = {
        jobRequisitionId: v.jobRequisitionId!,
        previousTenureYears: v.previousTenureYears ?? undefined,
        recruiterId: v.recruiterId!,
        status: v.status as UpdateBoomerangApplicationRequest['status'],
      };
      this.service.update(this.data.applicationId, request).subscribe({
        next: () => this.done('Application updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateBoomerangApplicationRequest = {
        alumniId: v.alumniId!,
        jobRequisitionId: v.jobRequisitionId!,
        previousTenureYears: v.previousTenureYears ?? undefined,
        recruiterId: v.recruiterId!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Application created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
