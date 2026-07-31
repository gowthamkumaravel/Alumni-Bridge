import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  ALUMNI_REFERRAL_STATUSES,
  AlumniReferralStatus,
} from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { AlumniReferralFormDialogComponent } from './alumni-referral-form-dialog.component';
import { AlumniReferralResponse } from './alumni-referral.model';
import { AlumniReferralsService } from './alumni-referrals.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-alumni-referral-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Referrals" subtitle="Candidate referrals submitted by alumni">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Referral
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Candidate, alumni or job ID" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
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
          <ng-container matColumnDef="referralId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.referralId }}</td>
          </ng-container>
          <ng-container matColumnDef="candidateName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Candidate</th>
            <td mat-cell *matCellDef="let row">{{ row.candidateName }}</td>
          </ng-container>
          <ng-container matColumnDef="candidateEmail">
            <th mat-header-cell *matHeaderCellDef>Email</th>
            <td mat-cell *matCellDef="let row">{{ row.candidateEmail }}</td>
          </ng-container>
          <ng-container matColumnDef="alumniName">
            <th mat-header-cell *matHeaderCellDef>Referred By</th>
            <td mat-cell *matCellDef="let row">{{ row.alumniName || 'Alumni #' + row.alumniId }}</td>
          </ng-container>
          <ng-container matColumnDef="jobRequisitionId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Job Req.</th>
            <td mat-cell *matCellDef="let row">{{ row.jobRequisitionId }}</td>
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
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No referrals found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class AlumniReferralListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'referralId',
    'candidateName',
    'candidateEmail',
    'alumniName',
    'jobRequisitionId',
    'status',
    'actions',
  ];

  readonly statuses = ALUMNI_REFERRAL_STATUSES;
  readonly dataSource = new MatTableDataSource<AlumniReferralResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: AlumniReferralStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: AlumniReferralsService,
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
        `${row.candidateName} ${row.candidateEmail} ${row.alumniName ?? ''} ${row.jobRequisitionId}`
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

  edit(row: AlumniReferralResponse): void {
    this.openForm(row);
  }

  changeStatus(row: AlumniReferralResponse, status: AlumniReferralStatus): void {
    this.service.changeStatus(row.referralId, status).subscribe(() => {
      this.toast.success('Referral status updated.');
      this.load();
    });
  }

  remove(row: AlumniReferralResponse): void {
    this.confirm
      .confirm({
        title: 'Delete referral',
        message: `Delete referral for ${row.candidateName}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.referralId).subscribe(() => {
            this.toast.success('Referral deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: AlumniReferralStatus): string {
    switch (status) {
      case 'HIRED':
        return 'green';
      case 'NOT_PROGRESSED':
        return 'red';
      case 'PROGRESSED':
      case 'CONTACTED':
        return 'blue';
      default:
        return 'amber';
    }
  }

  private openForm(row: AlumniReferralResponse | null): void {
    this.dialog
      .open(AlumniReferralFormDialogComponent, { data: row, width: '560px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
