import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Observable } from 'rxjs';

import { EVENT_STATUSES, EventStatus } from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { AlumniEventFormDialogComponent } from '../alumni-event-form-dialog/alumni-event-form-dialog.component';
import { AlumniEventResponse } from '../alumni-event.model';
import { AlumniEventsService } from '../alumni-events.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-alumni-event-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './alumni-event-list.component.html',})
export class AlumniEventListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'eventId',
    'title',
    'eventType',
    'eventDate',
    'venue',
    'rsvpCount',
    'status',
    'actions',
  ];

  readonly statuses = EVENT_STATUSES;
  readonly dataSource = new MatTableDataSource<AlumniEventResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: EventStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: AlumniEventsService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  get canManage(): boolean {
    return this.auth.hasAnyRole(['ADMIN', 'ALUMNI_RELATIONS']);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (row, raw) => {
      const { text, status } = JSON.parse(raw) as { text: string; status: string };
      const matchesText =
        !text ||
        `${row.title} ${row.venue ?? ''} ${row.targetCohort ?? ''}`
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

  edit(row: AlumniEventResponse): void {
    this.openForm(row);
  }

  publish(row: AlumniEventResponse): void {
    this.run(this.service.publish(row.eventId), 'Event published.');
  }

  complete(row: AlumniEventResponse): void {
    this.run(this.service.complete(row.eventId), 'Event marked complete.');
  }

  cancel(row: AlumniEventResponse): void {
    this.run(this.service.cancel(row.eventId), 'Event cancelled.');
  }

  rsvp(row: AlumniEventResponse): void {
    this.run(this.service.rsvp(row.eventId), 'RSVP recorded.');
  }

  remove(row: AlumniEventResponse): void {
    this.confirm
      .confirm({
        title: 'Delete event',
        message: `Delete "${row.title}"? This cannot be undone.`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.eventId).subscribe(() => {
            this.toast.success('Event deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: EventStatus): string {
    switch (status) {
      case 'PUBLISHED':
        return 'green';
      case 'CANCELLED':
        return 'red';
      case 'COMPLETED':
        return 'blue';
      default:
        return 'amber';
    }
  }

  private run(obs: Observable<AlumniEventResponse>, message: string): void {
    obs.subscribe(() => {
      this.toast.success(message);
      this.load();
    });
  }

  private openForm(row: AlumniEventResponse | null): void {
    this.dialog
      .open(AlumniEventFormDialogComponent, { data: row, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
