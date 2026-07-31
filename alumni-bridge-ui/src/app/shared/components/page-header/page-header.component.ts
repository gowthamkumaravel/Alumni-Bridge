import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/** Standard title + subtitle + action-slot header used at the top of each screen. */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-header">
      <div>
        <h1>{{ title }}</h1>
        <p class="subtitle" *ngIf="subtitle">{{ subtitle }}</p>
      </div>
      <div class="actions">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styles: [
    `
      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;
        gap: 16px;
        flex-wrap: wrap;
      }
      h1 {
        margin: 0;
        font-size: 22px;
        font-weight: 500;
      }
      .subtitle {
        margin: 4px 0 0;
        color: #777;
        font-size: 13px;
      }
      .actions {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    `,
  ],
})
export class PageHeaderComponent {
  @Input() title = '';
  @Input() subtitle?: string;
}
