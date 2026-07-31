import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import {
  ALUMNI_STATUSES,
  AlumniStatus,
} from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { DetailDialogComponent } from '../../../shared/components/detail-dialog/detail-dialog.component';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { AlumniProfileFormDialogComponent } from '../alumni-profile-form-dialog/alumni-profile-form-dialog.component';
import { AlumniProfileResponse } from '../alumni-profile.model';
import { AlumniProfilesService } from '../alumni-profiles.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-alumni-profile-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './alumni-profile-list.component.html',})
export class AlumniProfileListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'alumniId',
    'name',
    'formerEmployeeId',
    'currentOrganisation',
    'engagementTier',
    'status',
    'actions',
  ];

  readonly statuses = ALUMNI_STATUSES;
  readonly dataSource = new MatTableDataSource<AlumniProfileResponse>([]);
  loading = false;

  filterText = '';
  statusFilter: AlumniStatus | 'ALL' = 'ALL';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly service: AlumniProfilesService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  get canManage(): boolean {
    return this.auth.hasAnyRole(['ADMIN', 'HRBP', 'ALUMNI_RELATIONS']);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (row, raw) => {
      const { text, status } = JSON.parse(raw) as { text: string; status: string };
      const matchesText =
        !text ||
        `${row.name ?? ''} ${row.formerEmployeeId ?? ''} ${row.currentOrganisation ?? ''}`
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

  edit(row: AlumniProfileResponse): void {
    this.openForm(row);
  }

  deactivate(row: AlumniProfileResponse): void {
    this.confirm
      .confirm({
        title: 'Deactivate profile',
        message: `Deactivate alumni profile for ${row.name || 'this user'}?`,
        confirmText: 'Deactivate',
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.deactivate(row.alumniId).subscribe(() => {
            this.toast.success('Profile deactivated.');
            this.load();
          });
        }
      });
  }

  view(row: AlumniProfileResponse): void {
    this.dialog.open(DetailDialogComponent, {
      width: '520px',
      data: {
        title: `Alumni Profile #${row.alumniId}`,
        rows: [
          { label: 'Name', value: row.name },
          { label: 'Email', value: row.email },
          { label: 'Former Employee ID', value: row.formerEmployeeId },
          { label: 'Last Role', value: row.lastRole },
          { label: 'Departure Date', value: row.departureDateFrom },
          { label: 'Current Organisation', value: row.currentOrganisation },
          { label: 'Current Role', value: row.currentRole },
          { label: 'LinkedIn', value: row.linkedInRef },
          { label: 'Engagement Tier', value: row.engagementTier },
          { label: 'Community Opt-in', value: row.communityOptIn ? 'Yes' : 'No' },
          { label: 'Status', value: row.status },
        ],
      },
    });
  }

  remove(row: AlumniProfileResponse): void {
    this.confirm
      .confirm({
        title: 'Delete profile',
        message: `Delete alumni profile #${row.alumniId}? This cannot be undone.`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.service.delete(row.alumniId).subscribe(() => {
            this.toast.success('Profile deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: AlumniStatus): string {
    return status === 'ACTIVE' ? 'green' : 'red';
  }

  private openForm(row: AlumniProfileResponse | null): void {
    this.dialog
      .open(AlumniProfileFormDialogComponent, { data: row, width: '680px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
