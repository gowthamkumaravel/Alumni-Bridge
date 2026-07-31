import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ToastService } from '../../../core/services/toast.service';
import { ExitInterviewResponse } from '../exit-interview.model';
import { ExitInterviewsService } from '../exit-interviews.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-exit-interview-complete-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>Complete Exit Interview</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="completeForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline">
          <mat-label>Primary Reason for Leaving</mat-label>
          <input matInput formControlName="primaryReasonForLeaving" />
          <mat-error>This field is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Key Feedback</mat-label>
          <textarea matInput rows="4" formControlName="keyFeedback"></textarea>
          <mat-error>This field is required</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="completeForm" [disabled]="saving">
        Mark Completed
      </button>
    </mat-dialog-actions>
  `,
})
export class ExitInterviewCompleteDialogComponent {
  saving = false;

  readonly form = this.fb.group({
    primaryReasonForLeaving: ['', Validators.required],
    keyFeedback: ['', Validators.required],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: ExitInterviewsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<ExitInterviewCompleteDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: ExitInterviewResponse,
  ) {
    if (data.primaryReasonForLeaving || data.keyFeedback) {
      this.form.patchValue({
        primaryReasonForLeaving: data.primaryReasonForLeaving ?? '',
        keyFeedback: data.keyFeedback ?? '',
      });
    }
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    this.service
      .complete(this.data.interviewId, {
        primaryReasonForLeaving: this.form.value.primaryReasonForLeaving!,
        keyFeedback: this.form.value.keyFeedback!,
      })
      .subscribe({
        next: () => {
          this.toast.success('Exit interview completed.');
          this.dialogRef.close(true);
        },
        error: () => (this.saving = false),
      });
  }
}
