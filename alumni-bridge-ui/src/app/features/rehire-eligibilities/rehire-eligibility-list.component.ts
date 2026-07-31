import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Observable } from 'rxjs';

import {
  REHIRE_ELIGIBILITY_STATUSES,
  RehireEligibilityStatus,
} from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { RehireEligibilityFormDialogComponent } from './rehire-eligibility-form-dialog.component';
import { RehireEligibilityResponse } from './rehire-eligibility.model';
import { RehireEligibilitiesService } from './rehire-eligibilities.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-rehire-eligibility-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Rehire Eligibility" subtitle="Review whether alumni can be rehired">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Review
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Alumni or reviewer" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
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
          <ng-container matColumnDef="eligibilityId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.eligibilityId }}</td>
          </ng-container>
          <ng-container matColumnDef="alumniName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Alumni</th>
            <td mat-cell *matCellDef="let row">{{ row.alumniName || 'Alumni #' + row.alumniId }}</td>
          </ng-container>
          <ng-container matColumnDef="eligibleForRehire">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Eligible</th>
            <td mat-cell *matCellDef="let row">{{ row.eligibleForRehire | enumLabel }}</td>
          </ng-container>
          <ng-container matColumnDef="reviewedByName">
            <th mat-header-cell *matHeaderCellDef>Reviewer</th>
            <td mat-cell *matCellDef="let row">{{ row.reviewedByName || 'User #' + row.reviewedById }}</td>
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
                <button mat-menu-item (click)="markEligible(row)"><mat-icon>check_circle</mat-icon><span>Mark eligible</span></button>
                <button mat-menu-item (click)="markIneligible(row)"><mat-icon>cancel</mat-icon><span>Mark ineligible</span></button>
                <button mat-menu-item (click)="underReview(row)"><mat-icon>hourglass_empty</mat-icon><span>Under review</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No records found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class RehireEligibilityListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'eligibilityId',
    'alumniName',
    'eligibleForRehire',
    'reviewedByName',
    'status',
    'actions',
  ];

  readonly statuses = REHIRE_ELIGIBILITY_STATUSES;
  readonly dataSource = new MatTableDataSource<RehireEligibilityResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: RehireEligibilityStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: RehireEligibilitiesService,
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
        `${row.alumniName ?? ''} ${row.reviewedByName ?? ''}`
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

  edit(row: RehireEligibilityResponse): void {
    this.openForm(row);
  }

  markEligible(row: RehireEligibilityResponse): void {
    this.run(this.service.markEligible(row.eligibilityId), 'Marked eligible.');
  }

  markIneligible(row: RehireEligibilityResponse): void {
    this.run(this.service.markIneligible(row.eligibilityId), 'Marked ineligible.');
  }

  underReview(row: RehireEligibilityResponse): void {
    this.run(this.service.underReview(row.eligibilityId), 'Moved to under review.');
  }

  remove(row: RehireEligibilityResponse): void {
    this.confirm
      .confirm({
        title: 'Delete record',
        message: `Delete rehire eligibility #${row.eligibilityId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.eligibilityId).subscribe(() => {
            this.toast.success('Record deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: RehireEligibilityStatus): string {
    switch (status) {
      case 'ELIGIBLE':
        return 'green';
      case 'INELIGIBLE':
        return 'red';
      default:
        return 'amber';
    }
  }

  private run(obs: Observable<RehireEligibilityResponse>, message: string): void {
    obs.subscribe(() => {
      this.toast.success(message);
      this.load();
    });
  }

  private openForm(row: RehireEligibilityResponse | null): void {
    this.dialog
      .open(RehireEligibilityFormDialogComponent, { data: row, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
