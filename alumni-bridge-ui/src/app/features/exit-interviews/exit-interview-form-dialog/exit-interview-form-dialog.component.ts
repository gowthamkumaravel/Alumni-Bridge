import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import {
  EXIT_INTERVIEW_STATUSES,
  INTERVIEW_FORMATS,
} from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import {
  CreateExitInterviewRequest,
  ExitInterviewResponse,
  UpdateExitInterviewRequest,
} from '../exit-interview.model';
import { ExitInterviewsService } from '../exit-interviews.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-exit-interview-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Exit Interview' : 'Schedule Exit Interview' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="eiForm" (ngSubmit)="save()">
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Case ID</mat-label>
            <input matInput type="number" formControlName="caseId" />
            <mat-error>Case ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Interviewer (Employee ID)</mat-label>
            <input matInput formControlName="interviewerId" />
            <mat-error>Interviewer is required</mat-error>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Scheduled Date &amp; Time</mat-label>
            <input matInput type="datetime-local" formControlName="scheduledDate" />
            <mat-error>Scheduled date is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Format</mat-label>
            <mat-select formControlName="format">
              <mat-option *ngFor="let f of formats" [value]="f">{{ f | enumLabel }}</mat-option>
            </mat-select>
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
      <button mat-raised-button color="primary" type="submit" form="eiForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Schedule' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class ExitInterviewFormDialogComponent {
  readonly formats = INTERVIEW_FORMATS;
  readonly statuses = EXIT_INTERVIEW_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    caseId: [null as number | null, [Validators.required, Validators.min(1)]],
    interviewerId: ['' as string, [Validators.required]],
    scheduledDate: ['', Validators.required],
    format: ['IN_PERSON' as string, Validators.required],
    status: ['SCHEDULED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: ExitInterviewsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<ExitInterviewFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: ExitInterviewResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        caseId: data.caseId,
        interviewerId: data.interviewerId,
        scheduledDate: (data.scheduledDate || '').slice(0, 16),
        format: data.format,
        status: data.status,
      });
      this.form.get('caseId')?.disable();
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
      const request: UpdateExitInterviewRequest = {
        interviewerId: value.interviewerId!,
        scheduledDate: value.scheduledDate!,
        format: value.format as UpdateExitInterviewRequest['format'],
        status: value.status as UpdateExitInterviewRequest['status'],
      };
      this.service.update(this.data.interviewId, request).subscribe({
        next: () => this.done('Exit interview updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateExitInterviewRequest = {
        caseId: value.caseId!,
        interviewerId: value.interviewerId!,
        scheduledDate: value.scheduledDate!,
        format: value.format as CreateExitInterviewRequest['format'],
      };
      this.service.create(request).subscribe({
        next: () => this.done('Exit interview scheduled.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
