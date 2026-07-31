import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import {
  ELIGIBLE_FOR_REHIRE,
  REHIRE_ELIGIBILITY_STATUSES,
} from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import { toIsoDate } from '../../core/utils/date-utils';
import {
  CreateRehireEligibilityRequest,
  RehireEligibilityResponse,
  UpdateRehireEligibilityRequest,
} from './rehire-eligibility.model';
import { RehireEligibilitiesService } from './rehire-eligibilities.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-rehire-eligibility-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Rehire Eligibility' : 'New Rehire Eligibility' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="rehireForm" (ngSubmit)="save()">
        <div class="form-row">
          <mat-form-field appearance="outline" *ngIf="!isEdit">
            <mat-label>Alumni ID</mat-label>
            <input matInput type="number" formControlName="alumniId" />
            <mat-error>Alumni ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Reviewed By (User ID)</mat-label>
            <input matInput type="number" formControlName="reviewedById" />
            <mat-error>Reviewer is required</mat-error>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Eligible for Rehire</mat-label>
            <mat-select formControlName="eligibleForRehire">
              <mat-option *ngFor="let e of eligibleOptions" [value]="e">{{ e | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Cooling-off (months)</mat-label>
            <input matInput type="number" formControlName="coolingOffPeriodMonths" />
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Review Date</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="eligibilityReviewDate" />
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline" *ngIf="isEdit">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option *ngFor="let s of statuses" [value]="s">{{ s | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>Reason</mat-label>
          <textarea matInput rows="3" formControlName="eligibilityReason"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="rehireForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class RehireEligibilityFormDialogComponent {
  readonly eligibleOptions = ELIGIBLE_FOR_REHIRE;
  readonly statuses = REHIRE_ELIGIBILITY_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    alumniId: [null as number | null, [Validators.required, Validators.min(1)]],
    eligibleForRehire: ['YES' as string, Validators.required],
    coolingOffPeriodMonths: [null as number | null],
    eligibilityReviewDate: [null as Date | string | null],
    eligibilityReason: [''],
    reviewedById: [null as number | null, [Validators.required, Validators.min(1)]],
    status: ['UNDER_REVIEW' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: RehireEligibilitiesService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<RehireEligibilityFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: RehireEligibilityResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        eligibleForRehire: data.eligibleForRehire,
        coolingOffPeriodMonths: data.coolingOffPeriodMonths ?? null,
        eligibilityReviewDate: data.eligibilityReviewDate ?? null,
        eligibilityReason: data.eligibilityReason ?? '',
        reviewedById: data.reviewedById,
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
      const request: UpdateRehireEligibilityRequest = {
        eligibleForRehire: v.eligibleForRehire as UpdateRehireEligibilityRequest['eligibleForRehire'],
        coolingOffPeriodMonths: v.coolingOffPeriodMonths ?? undefined,
        eligibilityReviewDate: toIsoDate(v.eligibilityReviewDate),
        eligibilityReason: v.eligibilityReason || undefined,
        reviewedById: v.reviewedById!,
        status: v.status as UpdateRehireEligibilityRequest['status'],
      };
      this.service.update(this.data.eligibilityId, request).subscribe({
        next: () => this.done('Rehire eligibility updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateRehireEligibilityRequest = {
        alumniId: v.alumniId!,
        eligibleForRehire: v.eligibleForRehire as CreateRehireEligibilityRequest['eligibleForRehire'],
        coolingOffPeriodMonths: v.coolingOffPeriodMonths ?? undefined,
        eligibilityReviewDate: toIsoDate(v.eligibilityReviewDate),
        eligibilityReason: v.eligibilityReason || undefined,
        reviewedById: v.reviewedById!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Rehire eligibility created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
