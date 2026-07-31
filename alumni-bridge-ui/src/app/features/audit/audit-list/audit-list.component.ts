import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

import { AuditLogResponse } from '../audit-log.model';
import { AuditService } from '../audit.service';

interface AuditFilter {
  text: string;
  module: string;
  employeeId: string;
  from: string;
  to: string;
}

import { SHARED_IMPORTS } from '../../../shared/shared-imports';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './audit-list.component.html',})
export class AuditListComponent implements OnInit, AfterViewInit {
  readonly displayedColumns = [
    'auditId',
    'userName',
    'action',
    'module',
    'timestamp',
  ];

  readonly dataSource = new MatTableDataSource<AuditLogResponse>([]);
  loading = false;

  modules: string[] = [];

  readonly filter: AuditFilter = {
    text: '',
    module: 'ALL',
    employeeId: '',
    from: '',
    to: '',
  };

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private readonly service: AuditService) {}

  ngOnInit(): void {
    this.dataSource.filterPredicate = (row, raw) => this.matches(row, raw);
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
        this.modules = Array.from(new Set(rows.map((r) => r.module))).sort();
        this.loading = false;
        this.applyFilters();
      },
      error: () => (this.loading = false),
    });
  }

  applyFilters(): void {
    this.dataSource.filter = JSON.stringify(this.filter);
    this.dataSource.paginator?.firstPage();
  }

  clearFilters(): void {
    this.filter.text = '';
    this.filter.module = 'ALL';
    this.filter.employeeId = '';
    this.filter.from = '';
    this.filter.to = '';
    this.applyFilters();
  }

  private matches(row: AuditLogResponse, raw: string): boolean {
    const f = JSON.parse(raw) as AuditFilter;

    const matchesText =
      !f.text ||
      `${row.userName ?? ''} ${row.action} ${row.module}`
        .toLowerCase()
        .includes(f.text.toLowerCase());

    const matchesModule = f.module === 'ALL' || row.module === f.module;
    const matchesUser = !f.employeeId || (row.employeeId ?? '') === f.employeeId.trim();

    const ts = new Date(row.timestamp).getTime();
    const matchesFrom = !f.from || ts >= new Date(f.from).getTime();
    const matchesTo =
      !f.to || ts <= new Date(f.to).getTime() + 24 * 60 * 60 * 1000;

    return (
      matchesText && matchesModule && matchesUser && matchesFrom && matchesTo
    );
  }
}
