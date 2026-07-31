import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { HANDOVER_STATUSES, HandoverStatus } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { DetailDialogComponent } from '../../shared/components/detail-dialog/detail-dialog.component';
import { ConfirmService } from '../../shared/services/confirm.service';
import { KnowledgeHandoverFormDialogComponent } from './knowledge-handover-form-dialog.component';
import { KnowledgeHandoverResponse } from './knowledge-handover.model';
import { KnowledgeHandoversService } from './knowledge-handovers.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-knowledge-handover-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Knowledge Handovers" subtitle="Transfer of knowledge to successors">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Handover
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" class="search-field" subscriptSizing="dynamic">
            <mat-label>Search</mat-label>
            <input matInput placeholder="Employee or successor" [(ngModel)]="filterText" (ngModelChange)="applyFilters()" />
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
          <ng-container matColumnDef="handoverId">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.handoverId }}</td>
          </ng-container>
          <ng-container matColumnDef="employeeName">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Employee</th>
            <td mat-cell *matCellDef="let row">{{ row.employeeName || 'User #' + row.employeeId }}</td>
          </ng-container>
          <ng-container matColumnDef="successorName">
            <th mat-header-cell *matHeaderCellDef>Successor</th>
            <td mat-cell *matCellDef="let row">{{ row.successorName || 'User #' + row.successorId }}</td>
          </ng-container>
          <ng-container matColumnDef="completionDeadline">
            <th mat-header-cell *matHeaderCellDef mat-sort-header>Deadline</th>
            <td mat-cell *matCellDef="let row">{{ (row.completionDeadline | date: 'mediumDate') || '—' }}</td>
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
                <button mat-menu-item (click)="view(row)"><mat-icon>visibility</mat-icon><span>View</span></button>
                <button mat-menu-item (click)="edit(row)"><mat-icon>edit</mat-icon><span>Edit</span></button>
                <button mat-menu-item (click)="complete(row)" [disabled]="row.status === 'COMPLETED' || row.status === 'MANAGER_SIGNED'"><mat-icon>task_alt</mat-icon><span>Complete</span></button>
                <button mat-menu-item (click)="signoff(row)" [disabled]="row.status === 'MANAGER_SIGNED'"><mat-icon>approval</mat-icon><span>Manager sign-off</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No handovers found.</span></td>
          </tr>
        </table>

        <mat-paginator [pageSizeOptions]="[10, 25, 50]" pageSize="10" showFirstLastButtons></mat-paginator>
      </mat-card>
    </div>
  `,
  styles: ['.actions-col { width: 80px; text-align: center; }'],
})
export class KnowledgeHandoverListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'handoverId',
    'employeeName',
    'successorName',
    'completionDeadline',
    'status',
    'actions',
  ];

  readonly statuses = HANDOVER_STATUSES;
  readonly dataSource = new MatTableDataSource<KnowledgeHandoverResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: HandoverStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: KnowledgeHandoversService,
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
        `${row.employeeName ?? ''} ${row.successorName ?? ''}`
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

  edit(row: KnowledgeHandoverResponse): void {
    this.openForm(row);
  }

  complete(row: KnowledgeHandoverResponse): void {
    this.service.complete(row.handoverId).subscribe(() => {
      this.toast.success('Handover completed.');
      this.load();
    });
  }

  signoff(row: KnowledgeHandoverResponse): void {
    this.service.managerSignoff(row.handoverId).subscribe(() => {
      this.toast.success('Handover signed off.');
      this.load();
    });
  }

  view(row: KnowledgeHandoverResponse): void {
    this.dialog.open(DetailDialogComponent, {
      width: '560px',
      data: {
        title: `Knowledge Handover #${row.handoverId}`,
        rows: [
          { label: 'Case ID', value: row.caseId },
          { label: 'Employee', value: row.employeeName },
          { label: 'Successor', value: row.successorName },
          { label: 'Active Projects', value: row.activeProjectsSummary },
          { label: 'Key Contacts', value: row.keyContactsRef },
          { label: 'Systems Access', value: row.systemsAccessDetails },
          { label: 'Critical Notes', value: row.criticalProcessNotes },
          { label: 'Deadline', value: row.completionDeadline },
          { label: 'Status', value: row.status },
        ],
      },
    });
  }

  remove(row: KnowledgeHandoverResponse): void {
    this.confirm
      .confirm({
        title: 'Delete handover',
        message: `Delete knowledge handover #${row.handoverId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.handoverId).subscribe(() => {
            this.toast.success('Handover deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: HandoverStatus): string {
    switch (status) {
      case 'MANAGER_SIGNED':
        return 'green';
      case 'COMPLETED':
        return 'blue';
      case 'IN_PROGRESS':
        return 'amber';
      default:
        return '';
    }
  }

  private openForm(row: KnowledgeHandoverResponse | null): void {
    this.dialog
      .open(KnowledgeHandoverFormDialogComponent, { data: row, width: '680px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
