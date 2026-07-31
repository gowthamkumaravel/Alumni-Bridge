import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  OFFBOARDING_STATUSES,
  OffboardingStatus,
} from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { DetailDialogComponent } from '../../../shared/components/detail-dialog/detail-dialog.component';
import { OffboardingCaseFormDialogComponent } from '../offboarding-case-form-dialog/offboarding-case-form-dialog.component';
import { OffboardingCaseResponse } from '../offboarding-case.model';
import { OffboardingCasesService } from '../offboarding-cases.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-offboarding-case-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './offboarding-case-list.component.html',})
export class OffboardingCaseListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'caseId',
    'employeeName',
    'departureType',
    'lastWorkingDate',
    'hrbpOwnerName',
    'status',
    'actions',
  ];

  readonly statuses = OFFBOARDING_STATUSES;
  readonly dataSource = new MatTableDataSource<OffboardingCaseResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: OffboardingStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: OffboardingCasesService,
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
      const { text, status } = JSON.parse(raw) as {
        text: string;
        status: string;
      };
      const matchesText =
        !text ||
        `${row.employeeName ?? ''} ${row.hrbpOwnerName ?? ''} ${row.caseId}`
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

  edit(row: OffboardingCaseResponse): void {
    this.openForm(row);
  }

  changeStatus(row: OffboardingCaseResponse, status: OffboardingStatus): void {
    this.service.changeStatus(row.caseId, status).subscribe(() => {
      this.toast.success('Status updated.');
      this.load();
    });
  }

  view(row: OffboardingCaseResponse): void {
    this.dialog.open(DetailDialogComponent, {
      width: '520px',
      data: {
        title: `Offboarding Case #${row.caseId}`,
        rows: [
          { label: 'Employee', value: row.employeeName },
          { label: 'Employee Email', value: row.employeeEmail },
          { label: 'Departure Type', value: row.departureType },
          { label: 'Last Working Date', value: row.lastWorkingDate },
          { label: 'HRBP Owner', value: row.hrbpOwnerName },
          { label: 'Status', value: row.status },
          { label: 'Created', value: row.createdAt },
          { label: 'Updated', value: row.updatedAt },
        ],
      },
    });
  }

  remove(row: OffboardingCaseResponse): void {
    this.confirm
      .confirm({
        title: 'Delete case',
        message: `Delete offboarding case #${row.caseId}? This cannot be undone.`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.caseId).subscribe(() => {
            this.toast.success('Case deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: OffboardingStatus): string {
    switch (status) {
      case 'COMPLETED':
        return 'green';
      case 'CANCELLED':
        return 'red';
      case 'CLEARANCE_PENDING':
        return 'amber';
      default:
        return 'blue';
    }
  }

  private openForm(row: OffboardingCaseResponse | null): void {
    this.dialog
      .open(OffboardingCaseFormDialogComponent, { data: row, width: '560px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
