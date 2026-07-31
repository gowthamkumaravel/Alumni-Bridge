import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { AlumniReportFormDialogComponent } from './alumni-report-form-dialog.component';
import { AlumniReportResponse } from './alumni-report.model';
import { AlumniReportsService } from './alumni-reports.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-alumni-report-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Alumni Reports" subtitle="Generated analytics snapshots">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> Generate Report
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Scope, metric or value" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <span class="spacer"></span>
          <button mat-stroked-button (click)="load()"><mat-icon>refresh</mat-icon> Refresh</button>
        </div>

        <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

        <table mat-table [dataSource]="dataSource" matSort class="data-table">
          <ng-container matColumnDef="reportId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.reportId }}</td>
          </ng-container>
          <ng-container matColumnDef="scope">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Scope</th>
            <td mat-cell *matCellDef="let row">{{ row.scope | enumLabel }}</td>
          </ng-container>
          <ng-container matColumnDef="metric">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Metric</th>
            <td mat-cell *matCellDef="let row">{{ row.metric | enumLabel }}</td>
          </ng-container>
          <ng-container matColumnDef="metricValue">
            <th mat-header-cell *matHeaderCellDef>Value</th>
            <td mat-cell *matCellDef="let row">{{ row.metricValue || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="generatedDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Generated</th>
            <td mat-cell *matCellDef="let row">{{ row.generatedDate | date: 'medium' }}</td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-col">Actions</th>
            <td mat-cell *matCellDef="let row" class="actions-col">
              <button mat-icon-button (click)="remove(row)" *ngIf="isAdmin" matTooltip="Delete">
                <mat-icon color="warn">delete</mat-icon>
              </button>
              <span *ngIf="!isAdmin" class="text-muted">—</span>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No reports found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class AlumniReportListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'reportId',
    'scope',
    'metric',
    'metricValue',
    'generatedDate',
    'actions',
  ];

  readonly dataSource = new MatTableDataSource<AlumniReportResponse>([]);
  loading = false;
  filterText = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: AlumniReportsService,
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
      `${row.scope} ${row.metric} ${row.metricValue ?? ''}`
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
    this.service.getAll().subscribe({
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
    this.dialog
      .open(AlumniReportFormDialogComponent, { width: '480px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  remove(row: AlumniReportResponse): void {
    this.confirm
      .confirm({
        title: 'Delete report',
        message: `Delete report #${row.reportId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.reportId).subscribe(() => {
            this.toast.success('Report deleted.');
            this.load();
          });
        }
      });
  }
}
