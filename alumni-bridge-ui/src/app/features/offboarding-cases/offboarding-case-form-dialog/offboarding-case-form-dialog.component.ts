import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import {
  DEPARTURE_TYPES,
  OFFBOARDING_STATUSES,
} from '../../../core/models/enums';
import { ToastService } from '../../../core/services/toast.service';
import { toIsoDate } from '../../../core/utils/date-utils';
import {
  CreateOffboardingCaseRequest,
  OffboardingCaseResponse,
  UpdateOffboardingCaseRequest,
} from '../offboarding-case.model';
import { OffboardingCasesService } from '../offboarding-cases.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-offboarding-case-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './offboarding-case-form-dialog.component.html',
})
export class OffboardingCaseFormDialogComponent {
  readonly departureTypes = DEPARTURE_TYPES;
  readonly statuses = OFFBOARDING_STATUSES;

  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    employeeId: [null as number | null, [Validators.required, Validators.min(1)]],
    departureType: ['RESIGNATION' as string, Validators.required],
    lastWorkingDate: [null as Date | string | null, Validators.required],
    hrbpOwnerId: [null as number | null, [Validators.required, Validators.min(1)]],
    status: ['INITIATED' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: OffboardingCasesService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<
      OffboardingCaseFormDialogComponent,
      boolean
    >,
    @Inject(MAT_DIALOG_DATA) public data: OffboardingCaseResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        employeeId: data.employeeId,
        departureType: data.departureType,
        lastWorkingDate: data.lastWorkingDate,
        hrbpOwnerId: data.hrbpOwnerId,
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
    const value = this.form.getRawValue();

    if (this.isEdit && this.data) {
      const request: UpdateOffboardingCaseRequest = {
        departureType: value.departureType as UpdateOffboardingCaseRequest['departureType'],
        lastWorkingDate: toIsoDate(value.lastWorkingDate),
        hrbpOwnerId: value.hrbpOwnerId!,
        status: value.status as UpdateOffboardingCaseRequest['status'],
      };
      this.service.update(this.data.caseId, request).subscribe({
        next: () => this.done('Offboarding case updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateOffboardingCaseRequest = {
        employeeId: value.employeeId!,
        departureType: value.departureType as CreateOffboardingCaseRequest['departureType'],
        lastWorkingDate: toIsoDate(value.lastWorkingDate)!,
        hrbpOwnerId: value.hrbpOwnerId!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Offboarding case created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
