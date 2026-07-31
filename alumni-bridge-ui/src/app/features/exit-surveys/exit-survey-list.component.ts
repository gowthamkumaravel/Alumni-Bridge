import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { DetailDialogComponent } from '../../shared/components/detail-dialog/detail-dialog.component';
import { ConfirmService } from '../../shared/services/confirm.service';
import { ExitSurveyFormDialogComponent } from './exit-survey-form-dialog.component';
import { ExitSurveyResponse } from './exit-survey.model';
import { ExitSurveysService } from './exit-surveys.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-exit-survey-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Exit Surveys" subtitle="Structured departure feedback scores">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Survey
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Employee or case" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <mat-checkbox class="anon-toggle" [(ngModel)]="anonymousOnly" (ngModelChange)="load()">Anonymous only</mat-checkbox>
          <span class="spacer"></span>
          <button mat-stroked-button (click)="load()"><mat-icon>refresh</mat-icon> Refresh</button>
        </div>

        <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

        <table mat-table [dataSource]="dataSource" matSort class="data-table">
          <ng-container matColumnDef="responseId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.responseId }}</td>
          </ng-container>
          <ng-container matColumnDef="employeeName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Employee</th>
            <td mat-cell *matCellDef="let row">{{ row.anonymised ? 'Anonymous' : (row.employeeName || 'User #' + row.employeeId) }}</td>
          </ng-container>
          <ng-container matColumnDef="caseId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Case</th>
            <td mat-cell *matCellDef="let row">{{ row.caseId }}</td>
          </ng-container>
          <ng-container matColumnDef="overallScore">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Overall</th>
            <td mat-cell *matCellDef="let row">{{ row.overallScore ?? '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="submittedDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Submitted</th>
            <td mat-cell *matCellDef="let row">{{ (row.submittedDate | date: 'medium') || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-col">Actions</th>
            <td mat-cell *matCellDef="let row" class="actions-col">
              <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="view(row)"><mat-icon>visibility</mat-icon><span>View</span></button>
                <button mat-menu-item (click)="edit(row)"><mat-icon>edit</mat-icon><span>Edit</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No surveys found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: [
    '.actions-col { width: 80px; text-align: center; }',
    '.anon-toggle { margin-left: 8px; }',
  ],
})
export class ExitSurveyListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'responseId',
    'employeeName',
    'caseId',
    'overallScore',
    'submittedDate',
    'actions',
  ];

  readonly dataSource = new MatTableDataSource<ExitSurveyResponse>([]);
  loading = false;
  filterText = '';
  anonymousOnly = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: ExitSurveysService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (row, filter) =>
      `${row.employeeName ?? ''} ${row.caseId} ${row.employeeId}`
        .toLowerCase()
        .includes(filter.trim().toLowerCase());
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  load(): void {
    this.loading = true;
    const source = this.anonymousOnly
      ? this.service.getAnonymous()
      : this.service.getAll();
    source.subscribe({
      next: (rows) => {
        this.dataSource.data = rows;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  applyFilters(): void {
    this.dataSource.filter = this.filterText;
    this.dataSource.paginator?.firstPage();
  }

  create(): void {
    this.openForm(null);
  }

  edit(row: ExitSurveyResponse): void {
    this.openForm(row);
  }

  view(row: ExitSurveyResponse): void {
    this.dialog.open(DetailDialogComponent, {
      width: '520px',
      data: {
        title: `Exit Survey #${row.responseId}`,
        rows: [
          { label: 'Case ID', value: row.caseId },
          { label: 'Employee', value: row.anonymised ? 'Anonymous' : row.employeeName },
          { label: 'Manager Satisfaction', value: row.managerSatisfactionScore },
          { label: 'Team Culture', value: row.teamCultureScore },
          { label: 'Career Growth', value: row.careerGrowthScore },
          { label: 'Compensation', value: row.compensationScore },
          { label: 'Overall', value: row.overallScore },
          { label: 'Feedback', value: row.openTextFeedback },
          { label: 'Submitted', value: row.submittedDate },
        ],
      },
    });
  }

  remove(row: ExitSurveyResponse): void {
    this.confirm
      .confirm({
        title: 'Delete survey',
        message: `Delete exit survey #${row.responseId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.responseId).subscribe(() => {
            this.toast.success('Survey deleted.');
            this.load();
          });
        }
      });
  }

  private openForm(row: ExitSurveyResponse | null): void {
    this.dialog
      .open(ExitSurveyFormDialogComponent, { data: row, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
