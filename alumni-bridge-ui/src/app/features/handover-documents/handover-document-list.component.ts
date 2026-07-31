import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';

import {
  DOCUMENT_STATUSES,
  DOCUMENT_TYPES,
  DocumentStatus,
  DocumentType,
} from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { ConfirmService } from '../../shared/services/confirm.service';
import { HandoverDocumentFormDialogComponent } from './handover-document-form-dialog.component';
import { HandoverDocumentResponse } from './handover-document.model';
import { HandoverDocumentsService } from './handover-documents.service';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-handover-document-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="page-container">
      <app-page-header title="Handover Documents" subtitle="Documents attached to knowledge handovers">
        <button mat-raised-button color="primary" (click)="create()">
          <mat-icon>add</mat-icon> Add Document
        </button>
      </app-page-header>

      <mat-card class="table-card">
        <div class="table-toolbar">
          <mat-form-field appearance="outline" subscriptSizing="dynamic">
            <mat-label>View by</mat-label>
            <mat-select [(ngModel)]="viewBy" (ngModelChange)="onViewByChange()">
              <mat-option value="status">Status</mat-option>
              <mat-option value="type">Document Type</mat-option>
              <mat-option value="handover">Handover ID</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" *ngIf="viewBy === 'status'">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="statusValue" (ngModelChange)="load()">
              <mat-option *ngFor="let s of statuses" [value]="s">{{ s | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" *ngIf="viewBy === 'type'">
            <mat-label>Type</mat-label>
            <mat-select [(ngModel)]="typeValue" (ngModelChange)="load()">
              <mat-option *ngFor="let t of types" [value]="t">{{ t | enumLabel }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="id-field" *ngIf="viewBy === 'handover'">
            <mat-label>Handover ID</mat-label>
            <input matInput type="number" [(ngModel)]="handoverIdValue" (keyup.enter)="load()" />
          </mat-form-field>
          <button mat-raised-button color="primary" *ngIf="viewBy === 'handover'" (click)="load()" [disabled]="!handoverIdValue">
            <mat-icon>search</mat-icon> Load
          </button>

          <span class="spacer"></span>
        </div>

        <mat-progress-bar *ngIf="loading" mode="indeterminate"></mat-progress-bar>

        <table mat-table [dataSource]="dataSource" class="data-table">
          <ng-container matColumnDef="docId">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let row">{{ row.docId }}</td>
          </ng-container>
          <ng-container matColumnDef="handoverId">
            <th mat-header-cell *matHeaderCellDef>Handover</th>
            <td mat-cell *matCellDef="let row">{{ row.handoverId }}</td>
          </ng-container>
          <ng-container matColumnDef="documentType">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let row">{{ row.documentType | enumLabel }}</td>
          </ng-container>
          <ng-container matColumnDef="filePath">
            <th mat-header-cell *matHeaderCellDef>File</th>
            <td mat-cell *matCellDef="let row">{{ row.filePath }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let row">
              <span class="status-chip" [ngClass]="row.status === 'REVIEWED_BY_SUCCESSOR' ? 'green' : 'amber'">{{ row.status | enumLabel }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef class="actions-col">Actions</th>
            <td mat-cell *matCellDef="let row" class="actions-col">
              <button mat-icon-button [matMenuTriggerFor]="menu"><mat-icon>more_vert</mat-icon></button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="edit(row)"><mat-icon>edit</mat-icon><span>Edit</span></button>
                <button mat-menu-item (click)="markReviewed(row)" [disabled]="row.status === 'REVIEWED_BY_SUCCESSOR'"><mat-icon>rate_review</mat-icon><span>Mark reviewed</span></button>
                <button mat-menu-item (click)="remove(row)" *ngIf="isAdmin"><mat-icon color="warn">delete</mat-icon><span>Delete</span></button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="no-data-row" [attr.colspan]="displayedColumns.length"><span *ngIf="!loading">No documents found.</span></td>
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
export class HandoverDocumentListComponent implements OnInit {
  readonly displayedColumns = [
    'docId',
    'handoverId',
    'documentType',
    'filePath',
    'status',
    'actions',
  ];

  readonly statuses = DOCUMENT_STATUSES;
  readonly types = DOCUMENT_TYPES;
  readonly dataSource = new MatTableDataSource<HandoverDocumentResponse>([]);
  loading = false;

  viewBy: 'status' | 'type' | 'handover' = 'status';
  statusValue: DocumentStatus = 'UPLOADED';
  typeValue: DocumentType = 'PROCESS_GUIDE';
  handoverIdValue: number | null = null;

  constructor(
    private readonly service: HandoverDocumentsService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  ngOnInit(): void {
    this.load();
  }

  onViewByChange(): void {
    if (this.viewBy !== 'handover') {
      this.load();
    }
  }

  load(): void {
    if (this.viewBy === 'handover' && !this.handoverIdValue) {
      this.dataSource.data = [];
      return;
    }
    this.loading = true;
    const source =
      this.viewBy === 'status'
        ? this.service.getByStatus(this.statusValue)
        : this.viewBy === 'type'
          ? this.service.getByType(this.typeValue)
          : this.service.getByHandover(this.handoverIdValue!);
    source.subscribe({
      next: (rows) => {
        this.dataSource.data = rows;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  create(): void {
    this.openForm(null);
  }

  edit(row: HandoverDocumentResponse): void {
    this.openForm(row);
  }

  markReviewed(row: HandoverDocumentResponse): void {
    this.service.markReviewed(row.docId).subscribe(() => {
      this.toast.success('Document marked reviewed.');
      this.load();
    });
  }

  remove(row: HandoverDocumentResponse): void {
    this.confirm
      .confirm({
        title: 'Delete document',
        message: `Delete document #${row.docId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.docId).subscribe(() => {
            this.toast.success('Document deleted.');
            this.load();
          });
        }
      });
  }

  private openForm(row: HandoverDocumentResponse | null): void {
    this.dialog
      .open(HandoverDocumentFormDialogComponent, { data: row, width: '560px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
