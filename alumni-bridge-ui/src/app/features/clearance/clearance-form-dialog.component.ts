import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

import { ToastService } from '../../core/services/toast.service';
import { CreateClearanceCertificateRequest } from './clearance-certificate.model';
import { ClearanceService } from './clearance.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-clearance-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>Issue Clearance Certificate</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="clearanceForm" (ngSubmit)="save()">
        <mat-form-field appearance="outline">
          <mat-label>Case ID</mat-label>
          <input matInput type="number" formControlName="caseId" />
          <mat-error>Case ID is required</mat-error>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Issued By (User ID)</mat-label>
          <input matInput type="number" formControlName="issuedById" />
          <mat-error>Issuer is required</mat-error>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="clearanceForm" [disabled]="saving">Issue</button>
    </mat-dialog-actions>
  `,
})
export class ClearanceFormDialogComponent {
  saving = false;

  readonly form = this.fb.group({
    caseId: [null as number | null, [Validators.required, Validators.min(1)]],
    issuedById: [null as number | null, [Validators.required, Validators.min(1)]],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: ClearanceService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<ClearanceFormDialogComponent, boolean>,
  ) {}

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const request: CreateClearanceCertificateRequest = {
      caseId: this.form.value.caseId!,
      issuedById: this.form.value.issuedById!,
    };
    this.service.create(request).subscribe({
      next: () => {
        this.toast.success('Clearance certificate issued.');
        this.dialogRef.close(true);
      },
      error: () => (this.saving = false),
    });
  }
}
