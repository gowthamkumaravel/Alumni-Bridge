import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { EVENT_STATUSES, EVENT_TYPES } from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import {
  AlumniEventResponse,
  CreateAlumniEventRequest,
  UpdateAlumniEventRequest,
} from '../alumni-event.model';
import { AlumniEventsService } from '../alumni-events.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-alumni-event-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Event' : 'New Event' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="aeForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" />
          <mat-error>Title is required</mat-error>
        </mat-form-field>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Event Type</mat-label>
            <mat-select formControlName="eventType">
              <mat-option *ngFor="let t of types" [value]="t">{{ t | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Event Date &amp; Time</mat-label>
            <input matInput type="datetime-local" formControlName="eventDate" />
            <mat-error>Event date is required</mat-error>
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Venue</mat-label>
            <input matInput formControlName="venue" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Target Cohort</mat-label>
            <input matInput formControlName="targetCohort" />
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
      <button mat-raised-button color="primary" type="submit" form="aeForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class AlumniEventFormDialogComponent {
  readonly types = EVENT_TYPES;
  readonly statuses = EVENT_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    title: ['', Validators.required],
    eventType: ['NETWORKING' as string, Validators.required],
    eventDate: ['', Validators.required],
    venue: [''],
    targetCohort: [''],
    status: ['PLANNED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: AlumniEventsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<AlumniEventFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: AlumniEventResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        title: data.title,
        eventType: data.eventType,
        eventDate: (data.eventDate || '').slice(0, 16),
        venue: data.venue ?? '',
        targetCohort: data.targetCohort ?? '',
        status: data.status,
      });
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
      const request: UpdateAlumniEventRequest = {
        title: v.title!,
        eventType: v.eventType as UpdateAlumniEventRequest['eventType'],
        eventDate: v.eventDate!,
        venue: v.venue || undefined,
        targetCohort: v.targetCohort || undefined,
        status: v.status as UpdateAlumniEventRequest['status'],
      };
      this.service.update(this.data.eventId, request).subscribe({
        next: () => this.done('Event updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateAlumniEventRequest = {
        title: v.title!,
        eventType: v.eventType as CreateAlumniEventRequest['eventType'],
        eventDate: v.eventDate!,
        venue: v.venue || undefined,
        targetCohort: v.targetCohort || undefined,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Event created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
