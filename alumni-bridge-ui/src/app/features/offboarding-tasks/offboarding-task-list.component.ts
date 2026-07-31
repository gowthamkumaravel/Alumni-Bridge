import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';

import { TaskStatus } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { OffboardingTaskFormDialogComponent } from './offboarding-task-form-dialog.component';
import { OffboardingTaskResponse } from './offboarding-task.model';
import { OffboardingTasksService } from './offboarding-tasks.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-offboarding-task-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Offboarding Tasks" subtitle="Tasks are scoped to a case or an assignee">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> New Task
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" subscriptSizing="dynamic">
            <mat-label>View by</mat-label>
            <mat-select [(ngModel)]="viewBy">
              <mat-option value="case">Case ID</mat-option>
              <mat-option value="assignee">Assignee User ID</mat-option>
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="id-field">
            <mat-label>{{ viewBy === 'case' ? 'Case ID' : 'User ID' }}</mat-label>
            <input matInput type="number" [(ngModel)]="lookupId" (keyup.enter)="load()" />
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="load()" [disabled]="!lookupId">
            <mat-icon>search</mat-icon> Load
          </button>
          <span class="spacer"></span>
        </div>

        <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

        <table mat-table [dataSource]="dataSource" class="data-table">
          <ng-container matColumnDef="taskId">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.taskId }}</td>
          </ng-container>
          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Category</th>
            <td mat-cell *matCellDef="let row">{{ row.category | enumLabel }}</td>
          </ng-container>
          <ng-container matColumnDef="taskDescription">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let row">{{ row.taskDescription }}</td>
          </ng-container>
          <ng-container matColumnDef="assignedToName">
            <th mat-header-cell *matHeaderCellDef>Assignee</th>
            <td mat-cell *matCellDef="let row">{{ row.assignedToName || 'User #' + row.assignedToId }}</td>
          </ng-container>
          <ng-container matColumnDef="dueDate">
            <th mat-header-cell *matHeaderCellDef>Due</th>
            <td mat-cell *matCellDef="let row">{{ row.dueDate | date: 'mediumDate' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
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
                <button mat-menu-item (click)="complete(row)" [disabled]="row.status === 'COMPLETED'"><mat-icon>task_alt</mat-icon><span>Complete</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length">
              <span *ngIf="!loading">{{ hasLoaded ? 'No tasks found.' : 'Enter an ID and press Load to view tasks.' }}</span>
            </td>
          </tr>
        </table>
      </mat-card>
    </div>
  `,
  styles: [
    '.actions-col { width: 80px; text-align: center; }',
    '.id-field { width: 140px; }',
  ],
})
export class OffboardingTaskListComponent {
  readonly displayedColumns = [
    'taskId',
    'category',
    'taskDescription',
    'assignedToName',
    'dueDate',
    'status',
    'actions',
  ];

  readonly dataSource = new MatTableDataSource<OffboardingTaskResponse>([]);
  loading = false;
  hasLoaded = false;

  viewBy: 'case' | 'assignee' = 'case';
  lookupId: number | null = null;

  constructor(
    private readonly service: OffboardingTasksService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  load(): void {
    if (!this.lookupId) {
      return;
    }
    this.loading = true;
    const source =
      this.viewBy === 'case'
        ? this.service.getByCase(this.lookupId)
        : this.service.getByAssignee(this.lookupId);
    source.subscribe({
      next: (rows) => {
        this.dataSource.data = rows;
        this.loading = false;
        this.hasLoaded = true;
      },
      error: () => {
        this.loading = false;
        this.hasLoaded = true;
      },
    });
  }

  create(): void {
    this.dialog
      .open(OffboardingTaskFormDialogComponent, {
        width: '640px',
        data: {
          task: null,
          defaultCaseId: this.viewBy === 'case' ? this.lookupId : null,
        },
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  edit(row: OffboardingTaskResponse): void {
    this.dialog
      .open(OffboardingTaskFormDialogComponent, {
        width: '640px',
        data: { task: row, defaultCaseId: null },
      })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  complete(row: OffboardingTaskResponse): void {
    this.service.complete(row.taskId).subscribe(() => {
      this.toast.success('Task completed.');
      this.load();
    });
  }

  remove(row: OffboardingTaskResponse): void {
    this.confirm
      .confirm({
        title: 'Delete task',
        message: `Delete task #${row.taskId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.taskId).subscribe(() => {
            this.toast.success('Task deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: TaskStatus): string {
    switch (status) {
      case 'COMPLETED':
        return 'green';
      case 'OVERDUE':
        return 'red';
      case 'WAIVED':
        return '';
      default:
        return 'amber';
    }
  }
}
