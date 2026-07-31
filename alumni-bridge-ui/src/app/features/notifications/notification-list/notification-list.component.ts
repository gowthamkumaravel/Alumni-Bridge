import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  NOTIFICATION_CATEGORIES,
  NOTIFICATION_STATUSES,
  NotificationCategory,
  NotificationStatus,
} from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { NotificationDetailDialogComponent } from '../notification-detail-dialog/notification-detail-dialog.component';
import { NotificationFormDialogComponent } from '../notification-form-dialog/notification-form-dialog.component';
import { NotificationResponse } from '../notification.model';
import { NotificationsService } from '../notifications.service';

interface NotificationFilter {
  text: string;
  category: NotificationCategory | 'ALL';
  status: NotificationStatus | 'ALL';
}

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-notification-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './notification-list.component.html',})
export class NotificationListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'notificationId',
    'userName',
    'category',
    'message',
    'status',
    'createdDate',
    'actions',
  ];

  readonly categories = NOTIFICATION_CATEGORIES;
  readonly statuses = NOTIFICATION_STATUSES;

  readonly dataSource = new MatTableDataSource<NotificationResponse>([]);
  loading = false;

  readonly filter: NotificationFilter = {
    text: '',
    category: 'ALL',
    status: 'ALL',
  };

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: NotificationsService,
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
      const f = JSON.parse(raw) as NotificationFilter;
      const matchesText =
        !f.text ||
        `${row.userName ?? ''} ${row.message} ${row.employeeId}`
          .toLowerCase()
          .includes(f.text.toLowerCase());
      const matchesCategory =
        f.category === 'ALL' || row.category === f.category;
      const matchesStatus = f.status === 'ALL' || row.status === f.status;
      return matchesText && matchesCategory && matchesStatus;
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
    this.dataSource.filter = JSON.stringify(this.filter);
    this.dataSource.paginator?.firstPage();
  }

  create(): void {
    this.dialog
      .open(NotificationFormDialogComponent, { width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }

  view(row: NotificationResponse): void {
    this.dialog.open(NotificationDetailDialogComponent, {
      data: row,
      width: '520px',
    });
  }

  markRead(row: NotificationResponse): void {
    this.service.markRead(row.notificationId).subscribe(() => {
      this.toast.success('Marked as read.');
      this.load();
    });
  }

  dismiss(row: NotificationResponse): void {
    this.service.dismiss(row.notificationId).subscribe(() => {
      this.toast.success('Notification dismissed.');
      this.load();
    });
  }

  remove(row: NotificationResponse): void {
    this.confirm
      .confirm({
        title: 'Delete notification',
        message: 'Delete this notification permanently?',
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.notificationId).subscribe(() => {
            this.toast.success('Notification deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: NotificationStatus): string {
    switch (status) {
      case 'UNREAD':
        return 'blue';
      case 'READ':
        return 'green';
      default:
        return '';
    }
  }
}
