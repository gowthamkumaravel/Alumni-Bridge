import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { DOCUMENT_STATUSES, DOCUMENT_TYPES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import {
  CreateHandoverDocumentRequest,
  HandoverDocumentResponse,
  UpdateHandoverDocumentRequest,
} from './handover-document.model';
import { HandoverDocumentsService } from './handover-documents.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-handover-document-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Document' : 'Add Handover Document' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="docForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline" *ngIf="!isEdit">
          <mat-label>Handover ID</mat-label>
          <input matInput type="number" formControlName="handoverId" />
          <mat-error>Handover ID is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Document Type</mat-label>
          <mat-select formControlName="documentType">
            <mat-option *ngFor="let t of types" [value]="t">{{ t | enumLabel }}</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>File Path / Reference</mat-label>
          <input matInput formControlName="filePath" />
          <mat-error>File path is required</mat-error>
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
      <button mat-raised-button color="primary" type="submit" form="docForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Add' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class HandoverDocumentFormDialogComponent {
  readonly types = DOCUMENT_TYPES;
  readonly statuses = DOCUMENT_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    handoverId: [null as number | null, [Validators.required, Validators.min(1)]],
    documentType: ['PROCESS_GUIDE' as string, Validators.required],
    filePath: ['', Validators.required],
    status: ['UPLOADED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: HandoverDocumentsService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<HandoverDocumentFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: HandoverDocumentResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        documentType: data.documentType,
        filePath: data.filePath,
        status: data.status,
      });
      this.form.get('handoverId')?.clearValidators();
      this.form.get('handoverId')?.updateValueAndValidity();
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
      const request: UpdateHandoverDocumentRequest = {
        documentType: v.documentType as UpdateHandoverDocumentRequest['documentType'],
        filePath: v.filePath!,
        status: v.status as UpdateHandoverDocumentRequest['status'],
      };
      this.service.update(this.data.docId, request).subscribe({
        next: () => this.done('Document updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateHandoverDocumentRequest = {
        handoverId: v.handoverId!,
        documentType: v.documentType as CreateHandoverDocumentRequest['documentType'],
        filePath: v.filePath!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Document added.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
