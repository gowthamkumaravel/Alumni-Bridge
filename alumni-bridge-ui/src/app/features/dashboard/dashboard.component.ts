import { Component, OnInit } from '@angular/core';

import { AuthResponse } from '../../core/models/auth.models';
import { humanizeEnum } from '../../core/models/enums';
import { AuthService } from '../../core/services/auth.service';
import { NAV_SECTIONS, NavItem } from '../../layout/nav-items';

import { SHARED_IMPORTS } from '../../shared/shared-imports';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './dashboard.component.html',})
export class DashboardComponent implements OnInit {
  user: AuthResponse | null = null;
  shortcuts: NavItem[] = [];

  constructor(private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.user = this.auth.snapshot;
    this.shortcuts = this.accessibleShortcuts();
  }

  get roleLabel(): string {
    return this.user ? humanizeEnum(this.user.role) : '';
  }

  /** Flatten the nav config down to the modules this role can open (skip dashboard itself). */
  private accessibleShortcuts(): NavItem[] {
    const role = this.user?.role;
    if (!role) {
      return [];
    }
    return NAV_SECTIONS.flatMap((section) => section.items)
      .filter((item) => item.route !== '/dashboard')
      .filter((item) => item.roles.includes(role));
  }
}
