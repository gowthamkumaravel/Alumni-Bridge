import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ALUMNI_REFERRAL_STATUSES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import {
  AlumniReferralResponse,
  CreateAlumniReferralRequest,
  UpdateAlumniReferralRequest,
} from './alumni-referral.model';
import { AlumniReferralsService } from './alumni-referrals.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-alumni-referral-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Referral' : 'New Referral' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="refForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline" *ngIf="!isEdit">
          <mat-label>Alumni ID</mat-label>
          <input matInput type="number" formControlName="alumniId" />
          <mat-error>Alumni ID is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Candidate Name</mat-label>
          <input matInput formControlName="candidateName" />
          <mat-error>Candidate name is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Candidate Email</mat-label>
          <input matInput type="email" formControlName="candidateEmail" />
          <mat-error *ngIf="form.get('candidateEmail')?.hasError('required')">Email is required</mat-error>
          <mat-error *ngIf="form.get('candidateEmail')?.hasError('email')">Enter a valid email</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Job Requisition ID</mat-label>
          <input matInput formControlName="jobRequisitionId" />
          <mat-error>Job requisition is required</mat-error>
        </mat-form-field>
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
      <button mat-raised-button color="primary" type="submit" form="refForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class AlumniReferralFormDialogComponent {
  readonly statuses = ALUMNI_REFERRAL_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    alumniId: [null as number | null, [Validators.required, Validators.min(1)]],
    candidateName: ['', Validators.required],
    candidateEmail: ['', [Validators.required, Validators.email]],
    jobRequisitionId: ['', Validators.required],
    status: ['SUBMITTED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: AlumniReferralsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<AlumniReferralFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: AlumniReferralResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        candidateName: data.candidateName,
        candidateEmail: data.candidateEmail,
        jobRequisitionId: data.jobRequisitionId,
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
      const request: UpdateAlumniReferralRequest = {
        candidateName: v.candidateName!,
        candidateEmail: v.candidateEmail!,
        jobRequisitionId: v.jobRequisitionId!,
        status: v.status as UpdateAlumniReferralRequest['status'],
      };
      this.service.update(this.data.referralId, request).subscribe({
        next: () => this.done('Referral updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateAlumniReferralRequest = {
        alumniId: v.alumniId!,
        candidateName: v.candidateName!,
        candidateEmail: v.candidateEmail!,
        jobRequisitionId: v.jobRequisitionId!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Referral created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
