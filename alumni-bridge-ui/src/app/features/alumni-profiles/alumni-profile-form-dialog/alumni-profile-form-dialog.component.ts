import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ALUMNI_STATUSES, ENGAGEMENT_TIERS } from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import { toIsoDate } from '../../../core/utils/date-utils';
import {
  AlumniProfileResponse,
  CreateAlumniProfileRequest,
  UpdateAlumniProfileRequest,
} from '../alumni-profile.model';
import { AlumniProfilesService } from '../alumni-profiles.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-alumni-profile-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Alumni Profile' : 'New Alumni Profile' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="apForm" (ngSubmit)="save()">
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Employee ID</mat-label>
            <input matInput formControlName="employeeId" />
            <mat-error>Employee ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Last Role</mat-label>
            <input matInput formControlName="lastRole" />
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Current Organisation</mat-label>
            <input matInput formControlName="currentOrganisation" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Current Role</mat-label>
            <input matInput formControlName="currentRole" />
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline" *ngIf="!isEdit">
            <mat-label>Departure Date</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="departureDateFrom" />
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>LinkedIn Reference</mat-label>
            <input matInput formControlName="linkedInRef" />
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Engagement Tier</mat-label>
            <mat-select formControlName="engagementTier">
              <mat-option [value]="null">—</mat-option>
              <mat-option *ngFor="let t of tiers" [value]="t">{{ t | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" *ngIf="isEdit">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option *ngFor="let s of statuses" [value]="s">{{ s | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
        <mat-checkbox formControlName="communityOptIn">Community opt-in</mat-checkbox>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="apForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class AlumniProfileFormDialogComponent {
  readonly tiers = ENGAGEMENT_TIERS;
  readonly statuses = ALUMNI_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    employeeId: ['' as string, [Validators.required]],
    lastRole: [''],
    departureDateFrom: [null as Date | string | null],
    currentOrganisation: [''],
    currentRole: [''],
    linkedInRef: [''],
    engagementTier: [null as string | null],
    communityOptIn: [false],
    status: ['ACTIVE' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: AlumniProfilesService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<AlumniProfileFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: AlumniProfileResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        employeeId: data.employeeId,
        lastRole: data.lastRole ?? '',
        currentOrganisation: data.currentOrganisation ?? '',
        currentRole: data.currentRole ?? '',
        linkedInRef: data.linkedInRef ?? '',
        engagementTier: data.engagementTier ?? null,
        communityOptIn: data.communityOptIn ?? false,
        status: data.status,
      });
      this.form.get('employeeId')?.disable();
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
      const request: UpdateAlumniProfileRequest = {
        lastRole: v.lastRole || undefined,
        currentOrganisation: v.currentOrganisation || undefined,
        currentRole: v.currentRole || undefined,
        linkedInRef: v.linkedInRef || undefined,
        engagementTier: (v.engagementTier as UpdateAlumniProfileRequest['engagementTier']) || undefined,
        communityOptIn: v.communityOptIn ?? undefined,
        status: v.status as UpdateAlumniProfileRequest['status'],
      };
      this.service.update(this.data.alumniId, request).subscribe({
        next: () => this.done('Alumni profile updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateAlumniProfileRequest = {
        employeeId: v.employeeId!,
        lastRole: v.lastRole || undefined,
        departureDateFrom: toIsoDate(v.departureDateFrom),
        currentOrganisation: v.currentOrganisation || undefined,
        currentRole: v.currentRole || undefined,
        linkedInRef: v.linkedInRef || undefined,
        engagementTier: (v.engagementTier as CreateAlumniProfileRequest['engagementTier']) || undefined,
        communityOptIn: v.communityOptIn ?? undefined,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Alumni profile created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
