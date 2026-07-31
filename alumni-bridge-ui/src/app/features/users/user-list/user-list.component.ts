import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { UserRole, UserStatus } from '../../../core/models/enums';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { ConfirmService } from '../../../shared/services/confirm.service';
import { UserFormDialogComponent } from '../user-form-dialog/user-form-dialog.component';
import { UserResponse } from '../user.model';
import { UsersService } from '../users.service';

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './user-list.component.html',})
export class UserListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'employeeId',
    'name',
    'email',
    'role',
    'accountType',
    'status',
    'actions',
  ];

  readonly dataSource = new MatTableDataSource<UserResponse>([]);
  loading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private readonly usersService: UsersService,
    private readonly dialog: MatDialog,
    private readonly confirm: ConfirmService,
    private readonly toast: ToastService,
    private readonly auth: AuthService,
  ) {}

  get isAdmin(): boolean {
    return this.auth.hasAnyRole(['ADMIN']);
  }

  ngOnInit(): void {
    this.dataSource.filterPredicate = (user, filter) =>
      `${user.employeeId} ${user.name} ${user.email}`
        .toLowerCase()
        .includes(filter.trim().toLowerCase());
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  load(): void {
    this.loading = true;
    this.usersService.getAll().subscribe({
      next: (users) => {
        this.dataSource.data = users;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  applySearch(value: string): void {
    this.dataSource.filter = value;
    this.dataSource.paginator?.firstPage();
  }

  create(): void {
    this.openForm(null);
  }

  edit(user: UserResponse): void {
    this.openForm(user);
  }

  deactivate(user: UserResponse): void {
    this.confirm
      .confirm({
        title: 'Deactivate user',
        message: `Deactivate ${user.name}? They will no longer be able to sign in.`,
        confirmText: 'Deactivate',
      })
      .subscribe((ok) => {
        if (ok) {
          this.usersService.deactivate(user.employeeId).subscribe(() => {
            this.toast.success('User deactivated.');
            this.load();
          });
        }
      });
  }

  convertToAlumni(user: UserResponse): void {
    this.confirm
      .confirm({
        title: 'Convert to alumni',
        message: `Convert ${user.name} into an alumni account?`,
        confirmText: 'Convert',
      })
      .subscribe((ok) => {
        if (ok) {
          this.usersService.convertToAlumni(user.employeeId).subscribe(() => {
            this.toast.success('User converted to alumni.');
            this.load();
          });
        }
      });
  }

  remove(user: UserResponse): void {
    this.confirm
      .confirm({
        title: 'Delete user',
        message: `Permanently delete ${user.name}? This cannot be undone.`,
        confirmText: 'Delete',
        destructive: true,
      })
      .subscribe((ok) => {
        if (ok) {
          this.usersService.delete(user.employeeId).subscribe(() => {
            this.toast.success('User deleted.');
            this.load();
          });
        }
      });
  }

  statusClass(status: UserStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'green';
      case 'OFFBOARDING':
        return 'amber';
      case 'INACTIVE':
        return 'red';
      default:
        return 'blue';
    }
  }

  roleClass(role: UserRole): string {
    return role === 'ADMIN' ? 'blue' : '';
  }

  private openForm(user: UserResponse | null): void {
    this.dialog
      .open(UserFormDialogComponent, { data: user, width: '640px' })
      .afterClosed()
      .subscribe((saved) => {
        if (saved) {
          this.load();
        }
      });
  }
}
