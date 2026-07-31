import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { HANDOVER_STATUSES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import { toIsoDate } from '../../core/utils/date-utils';
import {
  CreateKnowledgeHandoverRequest,
  KnowledgeHandoverResponse,
  UpdateKnowledgeHandoverRequest,
} from './knowledge-handover.model';
import { KnowledgeHandoversService } from './knowledge-handovers.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-knowledge-handover-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Knowledge Handover' : 'New Knowledge Handover' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="khForm" (ngSubmit)="save()">
        <div class="form-row">
          <mat-form-field appearance="outline" *ngIf="!isEdit">
            <mat-label>Case ID</mat-label>
            <input matInput type="number" formControlName="caseId" />
            <mat-error>Case ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Successor (User ID)</mat-label>
            <input matInput type="number" formControlName="successorId" />
            <mat-error>Successor is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Completion Deadline</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="completionDeadline" />
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>Active Projects Summary</mat-label>
          <textarea matInput rows="2" formControlName="activeProjectsSummary"></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Key Contacts Reference</mat-label>
          <input matInput formControlName="keyContactsRef" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Systems Access Details</mat-label>
          <textarea matInput rows="2" formControlName="systemsAccessDetails"></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Critical Process Notes</mat-label>
          <textarea matInput rows="2" formControlName="criticalProcessNotes"></textarea>
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
      <button mat-raised-button color="primary" type="submit" form="khForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class KnowledgeHandoverFormDialogComponent {
  readonly statuses = HANDOVER_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    caseId: [null as number | null, [Validators.required, Validators.min(1)]],
    successorId: [null as number | null, [Validators.required, Validators.min(1)]],
    completionDeadline: [null as Date | string | null],
    activeProjectsSummary: [''],
    keyContactsRef: [''],
    systemsAccessDetails: [''],
    criticalProcessNotes: [''],
    status: ['DRAFT' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: KnowledgeHandoversService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<KnowledgeHandoverFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: KnowledgeHandoverResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        successorId: data.successorId,
        completionDeadline: data.completionDeadline ?? null,
        activeProjectsSummary: data.activeProjectsSummary ?? '',
        keyContactsRef: data.keyContactsRef ?? '',
        systemsAccessDetails: data.systemsAccessDetails ?? '',
        criticalProcessNotes: data.criticalProcessNotes ?? '',
        status: data.status,
      });
      this.form.get('caseId')?.clearValidators();
      this.form.get('caseId')?.updateValueAndValidity();
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
      const request: UpdateKnowledgeHandoverRequest = {
        successorId: v.successorId!,
        completionDeadline: toIsoDate(v.completionDeadline),
        activeProjectsSummary: v.activeProjectsSummary || undefined,
        keyContactsRef: v.keyContactsRef || undefined,
        systemsAccessDetails: v.systemsAccessDetails || undefined,
        criticalProcessNotes: v.criticalProcessNotes || undefined,
        status: v.status as UpdateKnowledgeHandoverRequest['status'],
      };
      this.service.update(this.data.handoverId, request).subscribe({
        next: () => this.done('Knowledge handover updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateKnowledgeHandoverRequest = {
        caseId: v.caseId!,
        successorId: v.successorId!,
        completionDeadline: toIsoDate(v.completionDeadline),
        activeProjectsSummary: v.activeProjectsSummary || undefined,
        keyContactsRef: v.keyContactsRef || undefined,
        systemsAccessDetails: v.systemsAccessDetails || undefined,
        criticalProcessNotes: v.criticalProcessNotes || undefined,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Knowledge handover created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
