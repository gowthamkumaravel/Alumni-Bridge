import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { TASK_CATEGORIES, TASK_STATUSES } from '../../core/models/enums';
import { ToastService } from '../../core/services/toast.service';
import { toIsoDate } from '../../core/utils/date-utils';
import {
  CreateOffboardingTaskRequest,
  OffboardingTaskResponse,
  UpdateOffboardingTaskRequest,
} from './offboarding-task.model';
import { OffboardingTasksService } from './offboarding-tasks.service';

interface DialogInput {
  task: OffboardingTaskResponse | null;
  defaultCaseId: number | null;
}

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-offboarding-task-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Task' : 'New Offboarding Task' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="taskForm" (ngSubmit)="save()">
        <div class="form-row">
          <mat-form-field appearance="outline" *ngIf="!isEdit">
            <mat-label>Case ID</mat-label>
            <input matInput type="number" formControlName="caseId" />
            <mat-error>Case ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline" *ngIf="!isEdit">
            <mat-label>Category</mat-label>
            <mat-select formControlName="category">
              <mat-option *ngFor="let c of categories" [value]="c">{{ c | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>Task Description</mat-label>
          <textarea matInput rows="3" formControlName="taskDescription"></textarea>
          <mat-error>Description is required</mat-error>
        </mat-form-field>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Assigned To (User ID)</mat-label>
            <input matInput type="number" formControlName="assignedToId" />
            <mat-error>Assignee is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Due Date</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="dueDate" />
            <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
            <mat-error>Due date is required</mat-error>
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
      <button mat-raised-button color="primary" type="submit" form="taskForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Create' }}
      </button>
    </mat-dialog-actions>
  `,
})
export class OffboardingTaskFormDialogComponent {
  readonly categories = TASK_CATEGORIES;
  readonly statuses = TASK_STATUSES;
  readonly isEdit: boolean;
  saving = false;

  readonly form = this.fb.group({
    caseId: [null as number | null, [Validators.required, Validators.min(1)]],
    category: ['IT' as string, Validators.required],
    taskDescription: ['', Validators.required],
    assignedToId: [null as number | null, [Validators.required, Validators.min(1)]],
    dueDate: [null as Date | string | null, Validators.required],
    status: ['PENDING' as string],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: OffboardingTasksService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<OffboardingTaskFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: DialogInput,
  ) {
    this.isEdit = !!data.task;
    if (data.task) {
      this.form.patchValue({
        caseId: data.task.caseId,
        category: data.task.category,
        taskDescription: data.task.taskDescription,
        assignedToId: data.task.assignedToId,
        dueDate: data.task.dueDate,
        status: data.task.status,
      });
    } else if (data.defaultCaseId) {
      this.form.patchValue({ caseId: data.defaultCaseId });
    }
  }

  save(): void {
    if (this.form.invalid || this.saving) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving = true;
    const v = this.form.getRawValue();

    if (this.isEdit && this.data.task) {
      const request: UpdateOffboardingTaskRequest = {
        taskDescription: v.taskDescription!,
        assignedToId: v.assignedToId!,
        dueDate: toIsoDate(v.dueDate),
        status: v.status as UpdateOffboardingTaskRequest['status'],
      };
      this.service.update(this.data.task.taskId, request).subscribe({
        next: () => this.done('Task updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: CreateOffboardingTaskRequest = {
        caseId: v.caseId!,
        category: v.category as CreateOffboardingTaskRequest['category'],
        taskDescription: v.taskDescription!,
        assignedToId: v.assignedToId!,
        dueDate: toIsoDate(v.dueDate)!,
      };
      this.service.create(request).subscribe({
        next: () => this.done('Task created.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
