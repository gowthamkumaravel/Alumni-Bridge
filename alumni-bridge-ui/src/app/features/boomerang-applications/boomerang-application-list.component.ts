import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  BOOMERANG_APPLICATION_STATUSES,
  BoomerangApplicationStatus,
} from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { BoomerangApplicationFormDialogComponent } from './boomerang-application-form-dialog.component';
import { BoomerangApplicationResponse } from './boomerang-application.model';
import { BoomerangApplicationsService } from './boomerang-applications.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-boomerang-application-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Boomerang Applications" subtitle="Alumni re-applying for open roles">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Application
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Alumni, recruiter or job ID" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
            <mat-icon matSuffix>search</mat-icon>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="statusFilter" (ngModelChange)="applyFilters()">
              <mat-option value="ALL">All statuses</mat-option>
              <mat-option *ngFor="let s of statuses" [value]="s">{{ s | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>
          <span class="spacer"></span>
          <button mat-stroked-button (click)="load()"><mat-icon>refresh</mat-icon> Refresh</button>
        </div>

        <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

        <table mat-table [dataSource]="dataSource" matSort class="data-table">
          <ng-container matColumnDef="applicationId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.applicationId }}</td>
          </ng-container>
          <ng-container matColumnDef="alumniName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Alumni</th>
            <td mat-cell *matCellDef="let row">{{ row.alumniName || 'Alumni #' + row.alumniId }}</td>
          </ng-container>
          <ng-container matColumnDef="jobRequisitionId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Job Req.</th>
            <td mat-cell *matCellDef="let row">{{ row.jobRequisitionId }}</td>
          </ng-container>
          <ng-container matColumnDef="recruiterName">
            <th mat-header-cell *matHeaderCellDef>Recruiter</th>
            <td mat-cell *matCellDef="let row">{{ row.recruiterName || 'User #' + row.recruiterId }}</td>
          </ng-container>
          <ng-container matColumnDef="applicationDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Applied</th>
            <td mat-cell *matCellDef="let row">{{ (row.applicationDate | date: 'mediumDate') || '—' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Status</th>
            <td mat-cell *matCellDef="let row">
              <span class="status-chip" [ngClass]="statusClass(row.status)">{{ row.status | enumLabel }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-col">Actions</th>
            <td mat-cell *matCellDef="let row" class="actions-col">
              <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="edit(row)"><mat-icon>edit</mat-icon><span>Edit</span></button>
                <button mat-menu-item [matMenuTriggerFor]="statusMenu"><mat-icon>swap_horiz</mat-icon><span>Change status</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
              <mat-menu #statusMenu="matMenu">
                <button mat-menu-item *ngFor="let s of statuses" (click)="changeStatus(row, s)">{{ s | enumLabel }}</button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No applications found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class BoomerangApplicationListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'applicationId',
    'alumniName',
    'jobRequisitionId',
    'recruiterName',
    'applicationDate',
    'status',
    'actions',
  ];

  readonly statuses = BOOMERANG_APPLICATION_STATUSES;
  readonly dataSource = new MatTableDataSource<BoomerangApplicationResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: BoomerangApplicationStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: BoomerangApplicationsService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (row, raw) => {
      const { text, status } = JSON.parse(raw) as { text: string; status: string };
      const matchesText =
        !text ||
        `${row.alumniName ?? ''} ${row.recruiterName ?? ''} ${row.jobRequisitionId}`
          .toLowerCase()
          .includes(text.toLowerCase());
      const matchesStatus = status === 'ALL' || row.status === status;
      return matchesText && matchesStatus;
    };
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
    this.dataSource.filter = JSON.stringify({
      text: this.filterText,
      status: this.statusFilter,
    });
    this.dataSource.paginator?.firstPage();
  }

  create(): void {
    this.openForm(null);
  }

  edit(row: BoomerangApplicationResponse): void {
    this.openForm(row);
  }

  changeStatus(
    row: BoomerangApplicationResponse,
    status: BoomerangApplicationStatus,
  ): void {
    this.service.changeStatus(row.applicationId, status).subscribe(() => {
      this.toast.success('Application status updated.');
      this.load();
    });
  }

  remove(row: BoomerangApplicationResponse): void {
    this.confirm
      .confirm({
        title: 'Delete application',
        message: `Delete application #${row.applicationId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.applicationId).subscribe(() => {
            this.toast.success('Application deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: BoomerangApplicationStatus): string {
    switch (status) {
      case 'JOINED':
        return 'green';
      case 'DECLINED':
        return 'red';
      case 'OFFERED':
      case 'INTERVIEWED':
        return 'blue';
      default:
        return 'amber';
    }
  }

  private openForm(row: BoomerangApplicationResponse | null): void {
    this.dialog
      .open(BoomerangApplicationFormDialogComponent, { data: row, width: '560px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
