import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Shown inside tables/cards when there is nothing to display. */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="empty-state">
      <mat-icon>{{ icon }}</mat-icon>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [
    `
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px 16px;
        color: #9e9e9e;
      }
      mat-icon {
        font-size: 40px;
        height: 40px;
        width: 40px;
        margin-bottom: 8px;
      }
      p {
        margin: 0;
        font-size: 14px;
      }
    `,
  ],
})
export class EmptyStateComponent {
  @Input() message = 'No records found.';
  @Input() icon = 'inbox';
}
