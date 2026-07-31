import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { CLEARANCE_STATUSES, ClearanceStatus } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { ClearanceCertificateResponse } from './clearance-certificate.model';
import { ClearanceFormDialogComponent } from './clearance-form-dialog.component';
import { ClearanceService } from './clearance.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-clearance-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Clearance Certificates" subtitle="Departure clearance status per case">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> Issue Certificate
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Employee, issuer or case" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
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
          <ng-container matColumnDef="certId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.certId }}</td>
          </ng-container>
          <ng-container matColumnDef="caseId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Case</th>
            <td mat-cell *matCellDef="let row">{{ row.caseId }}</td>
          </ng-container>
          <ng-container matColumnDef="employeeName">
            <th mat-header-cell *matHeaderCellDef>Employee</th>
            <td mat-cell *matCellDef="let row">{{ row.employeeName || 'User #' + row.employeeId }}</td>
          </ng-container>
          <ng-container matColumnDef="issuedByName">
            <th mat-header-cell *matHeaderCellDef>Issued By</th>
            <td mat-cell *matCellDef="let row">{{ row.issuedByName || 'User #' + row.issuedById }}</td>
          </ng-container>
          <ng-container matColumnDef="issuedDate">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Issued</th>
            <td mat-cell *matCellDef="let row">{{ (row.issuedDate | date: 'mediumDate') || '—' }}</td>
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
              <button mat-icon-button (click)="remove(row)" *ngIf="isAdmin" matTooltip="Delete">
                <mat-icon color="warn">delete</mat-icon>
              </button>
              <span *ngIf="!isAdmin" class="text-muted">—</span>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No certificates found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class ClearanceListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'certId',
    'caseId',
    'employeeName',
    'issuedByName',
    'issuedDate',
    'status',
    'actions',
  ];

  readonly statuses = CLEARANCE_STATUSES;
  readonly dataSource = new MatTableDataSource<ClearanceCertificateResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: ClearanceStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: ClearanceService,
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
        `${row.employeeName ?? ''} ${row.issuedByName ?? ''} ${row.caseId}`
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
    this.dialog
      .open(ClearanceFormDialogComponent, { width: '480px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  remove(row: ClearanceCertificateResponse): void {
    this.confirm
      .confirm({
        title: 'Delete certificate',
        message: `Delete clearance certificate #${row.certId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.certId).subscribe(() => {
            this.toast.success('Certificate deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: ClearanceStatus): string {
    switch (status) {
      case 'CLEAR':
        return 'green';
      case 'WITHHELD_DUE_TO_OUTSTANDING':
        return 'red';
      default:
        return 'amber';
    }
  }
}
