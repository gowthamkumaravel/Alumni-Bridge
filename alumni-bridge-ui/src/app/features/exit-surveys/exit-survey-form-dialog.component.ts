import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { ToastService } from '../../core/services/toast.service';
import {
  ExitSurveyResponse,
  SubmitExitSurveyRequest,
  UpdateExitSurveyRequest,
} from './exit-survey.model';
import { ExitSurveysService } from './exit-surveys.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-exit-survey-form-dialog',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <h2 mat-dialog-title>{{ isEdit ? 'Edit Exit Survey' : 'Submit Exit Survey' }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="dialog-form" id="surveyForm" (ngSubmit)="save()">
        <div class="form-row" *ngIf="!isEdit">
          <mat-form-field appearance="outline">
            <mat-label>Case ID</mat-label>
            <input matInput type="number" formControlName="caseId" />
            <mat-error>Case ID is required</mat-error>
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Employee ID</mat-label>
            <input matInput type="number" formControlName="employeeId" />
            <mat-error>Employee ID is required</mat-error>
          </mat-form-field>
        </div>
        <p class="hint">Scores are on a 1–5 scale.</p>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Manager Satisfaction</mat-label>
            <input matInput type="number" min="1" max="5" formControlName="managerSatisfactionScore" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Team Culture</mat-label>
            <input matInput type="number" min="1" max="5" formControlName="teamCultureScore" />
          </mat-form-field>
        </div>
        <div class="form-row">
          <mat-form-field appearance="outline">
            <mat-label>Career Growth</mat-label>
            <input matInput type="number" min="1" max="5" formControlName="careerGrowthScore" />
          </mat-form-field>
          <mat-form-field appearance="outline">
            <mat-label>Compensation</mat-label>
            <input matInput type="number" min="1" max="5" formControlName="compensationScore" />
          </mat-form-field>
        </div>
        <mat-form-field appearance="outline">
          <mat-label>Overall Score</mat-label>
          <input matInput type="number" min="1" max="5" formControlName="overallScore" />
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Feedback</mat-label>
          <textarea matInput rows="3" formControlName="openTextFeedback"></textarea>
        </mat-form-field>
        <mat-checkbox formControlName="anonymised">Submit anonymously</mat-checkbox>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving">Cancel</button>
      <button mat-raised-button color="primary" type="submit" form="surveyForm" [disabled]="saving">
        {{ isEdit ? 'Save Changes' : 'Submit' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: ['.hint { color: #777; font-size: 12px; margin: 0 0 4px; }'],
})
export class ExitSurveyFormDialogComponent {
  readonly isEdit: boolean;
  saving = false;

  private readonly scoreValidators = [Validators.min(1), Validators.max(5)];

  readonly form = this.fb.group({
    caseId: [null as number | null, [Validators.required, Validators.min(1)]],
    employeeId: [null as number | null, [Validators.required, Validators.min(1)]],
    managerSatisfactionScore: [null as number | null, this.scoreValidators],
    teamCultureScore: [null as number | null, this.scoreValidators],
    careerGrowthScore: [null as number | null, this.scoreValidators],
    compensationScore: [null as number | null, this.scoreValidators],
    overallScore: [null as number | null, this.scoreValidators],
    openTextFeedback: [''],
    anonymised: [false],
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: ExitSurveysService,
    private readonly toast: ToastService,
    private readonly dialogRef: MatDialogRef<ExitSurveyFormDialogComponent, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: ExitSurveyResponse | null,
  ) {
    this.isEdit = !!data;
    if (data) {
      this.form.patchValue({
        managerSatisfactionScore: data.managerSatisfactionScore ?? null,
        teamCultureScore: data.teamCultureScore ?? null,
        careerGrowthScore: data.careerGrowthScore ?? null,
        compensationScore: data.compensationScore ?? null,
        overallScore: data.overallScore ?? null,
        openTextFeedback: data.openTextFeedback ?? '',
        anonymised: data.anonymised ?? false,
      });
      this.form.get('caseId')?.clearValidators();
      this.form.get('employeeId')?.clearValidators();
      this.form.get('caseId')?.updateValueAndValidity();
      this.form.get('employeeId')?.updateValueAndValidity();
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
      const request: UpdateExitSurveyRequest = {
        managerSatisfactionScore: v.managerSatisfactionScore ?? undefined,
        teamCultureScore: v.teamCultureScore ?? undefined,
        careerGrowthScore: v.careerGrowthScore ?? undefined,
        compensationScore: v.compensationScore ?? undefined,
        overallScore: v.overallScore ?? undefined,
        openTextFeedback: v.openTextFeedback || undefined,
        anonymised: v.anonymised ?? undefined,
      };
      this.service.update(this.data.responseId, request).subscribe({
        next: () => this.done('Exit survey updated.'),
        error: () => (this.saving = false),
      });
    } else {
      const request: SubmitExitSurveyRequest = {
        caseId: v.caseId!,
        employeeId: v.employeeId!,
        managerSatisfactionScore: v.managerSatisfactionScore ?? undefined,
        teamCultureScore: v.teamCultureScore ?? undefined,
        careerGrowthScore: v.careerGrowthScore ?? undefined,
        compensationScore: v.compensationScore ?? undefined,
        overallScore: v.overallScore ?? undefined,
        openTextFeedback: v.openTextFeedback || undefined,
        anonymised: v.anonymised ?? undefined,
      };
      this.service.submit(request).subscribe({
        next: () => this.done('Exit survey submitted.'),
        error: () => (this.saving = false),
      });
    }
  }

  private done(message: string): void {
    this.toast.success(message);
    this.dialogRef.close(true);
  }
}
