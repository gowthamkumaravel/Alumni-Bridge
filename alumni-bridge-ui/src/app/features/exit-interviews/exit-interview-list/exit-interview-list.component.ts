import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  EXIT_INTERVIEW_STATUSES,
  ExitInterviewStatus,
} from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { DetailDialogComponent } from '../../../shared/components/detail-dialog/detail-dialog.component';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { ExitInterviewCompleteDialogComponent } from '../exit-interview-complete-dialog/exit-interview-complete-dialog.component';
import { ExitInterviewFormDialogComponent } from '../exit-interview-form-dialog/exit-interview-form-dialog.component';
import { ExitInterviewResponse } from '../exit-interview.model';
import { ExitInterviewsService } from '../exit-interviews.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-exit-interview-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './exit-interview-list.component.html',})
export class ExitInterviewListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'interviewId',
    'employeeName',
    'interviewerName',
    'scheduledDate',
    'format',
    'status',
    'actions',
  ];

  readonly statuses = EXIT_INTERVIEW_STATUSES;
  readonly dataSource = new MatTableDataSource<ExitInterviewResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: ExitInterviewStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: ExitInterviewsService,
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
        `${row.employeeName ?? ''} ${row.interviewerName ?? ''} ${row.interviewId}`
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

  edit(row: ExitInterviewResponse): void {
    this.openForm(row);
  }

  complete(row: ExitInterviewResponse): void {
    this.dialog
      .open(ExitInterviewCompleteDialogComponent, { data: row, width: '560px' })
      .afterClosed()
      .subscribe((done) => {
        if (done) {
          this.load();
        }
      });
  }

  view(row: ExitInterviewResponse): void {
    this.dialog.open(DetailDialogComponent, {
      width: '520px',
      data: {
        title: `Exit Interview #${row.interviewId}`,
        rows: [
          { label: 'Case ID', value: row.caseId },
          { label: 'Employee', value: row.employeeName },
          { label: 'Interviewer', value: row.interviewerName },
          { label: 'Scheduled', value: row.scheduledDate },
          { label: 'Format', value: row.format },
          { label: 'Status', value: row.status },
          { label: 'Reason for Leaving', value: row.primaryReasonForLeaving },
          { label: 'Key Feedback', value: row.keyFeedback },
        ],
      },
    });
  }

  remove(row: ExitInterviewResponse): void {
    this.confirm
      .confirm({
        title: 'Delete exit interview',
        message: `Delete exit interview #${row.interviewId}?`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.interviewId).subscribe(() => {
            this.toast.success('Exit interview deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: ExitInterviewStatus): string {
    switch (status) {
      case 'COMPLETED':
        return 'green';
      case 'NO_SHOW':
      case 'DECLINED':
        return 'red';
      default:
        return 'blue';
    }
  }

  private openForm(row: ExitInterviewResponse | null): void {
    this.dialog
      .open(ExitInterviewFormDialogComponent, { data: row, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
