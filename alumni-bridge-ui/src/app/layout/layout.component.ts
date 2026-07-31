import { Component, OnInit } from '@angular/core';

import { AuthResponse } from '../core/models/auth.models';
import { humanizeEnum } from '../core/models/enums';
import { AuthService } from '../core/services/auth.service';
import { ConfirmService } from '../shared/services/confirm.service';
import { NAV_SECTIONS, NavSection } from './nav-items';

import { SHARED_IMPORTS } from '../shared/shared-imports';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {
  user: AuthResponse | null = null;
  sections: NavSection[] = [];

  constructor(
    private readonly auth: AuthService,
    private readonly confirm: ConfirmService,
  ) {}

  ngOnInit(): void {
    this.user = this.auth.snapshot;
    this.sections = this.visibleSections();
  }

  roleLabel(): string {
    return this.user ? humanizeEnum(this.user.role) : '';
  }

  logout(): void {
    this.confirm
      .confirm({
        title: 'Sign out',
        message: 'Are you sure you want to sign out?',
        confirmText: 'Sign out',
      })
      .subscribe((confirmed) => {
        if (confirmed) {
          this.auth.logout();
        }
      });
  }

  /** Only show sections/items the current role can actually open. */
  private visibleSections(): NavSection[] {
    return NAV_SECTIONS.map((section) => ({
      heading: section.heading,
      items: section.items.filter(
        (item) => this.user && item.roles.includes(this.user.role),
      ),
    })).filter((section) => section.items.length > 0);
  }
}
