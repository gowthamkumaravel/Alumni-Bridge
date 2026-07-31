import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { LayoutComponent } from './layout/layout.component';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login.component').then(
        (m) => m.LoginComponent,
      ),
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent,
          ),
      },
      {
        path: 'users',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import('./features/users/user-list/user-list.component').then(
            (m) => m.UserListComponent,
          ),
      },
      {
        path: 'notifications',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/notifications/notification-list/notification-list.component'
          ).then((m) => m.NotificationListComponent),
      },
      {
        path: 'audit-logs',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () =>
          import('./features/audit/audit-list/audit-list.component').then(
            (m) => m.AuditListComponent,
          ),
      },
      {
        path: 'offboarding-cases',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import(
            './features/offboarding-cases/offboarding-case-list/offboarding-case-list.component'
          ).then((m) => m.OffboardingCaseListComponent),
      },
      {
        path: 'offboarding-tasks',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'IT_OPS'] },
        loadComponent: () =>
          import(
            './features/offboarding-tasks/offboarding-task-list.component'
          ).then((m) => m.OffboardingTaskListComponent),
      },
      {
        path: 'exit-interviews',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import(
            './features/exit-interviews/exit-interview-list/exit-interview-list.component'
          ).then((m) => m.ExitInterviewListComponent),
      },
      {
        path: 'exit-surveys',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import('./features/exit-surveys/exit-survey-list.component').then(
            (m) => m.ExitSurveyListComponent,
          ),
      },
      {
        path: 'clearance-certificates',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import('./features/clearance/clearance-list.component').then(
            (m) => m.ClearanceListComponent,
          ),
      },
      {
        path: 'knowledge-handovers',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import(
            './features/knowledge-handovers/knowledge-handover-list.component'
          ).then((m) => m.KnowledgeHandoverListComponent),
      },
      {
        path: 'handover-documents',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP'] },
        loadComponent: () =>
          import(
            './features/handover-documents/handover-document-list.component'
          ).then((m) => m.HandoverDocumentListComponent),
      },
      {
        path: 'alumni-profiles',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/alumni-profiles/alumni-profile-list/alumni-profile-list.component'
          ).then((m) => m.AlumniProfileListComponent),
      },
      {
        path: 'alumni-events',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'ALUMNI_RELATIONS', 'EMPLOYEE', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/alumni-events/alumni-event-list/alumni-event-list.component'
          ).then((m) => m.AlumniEventListComponent),
      },
      {
        path: 'alumni-referrals',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS'] },
        loadComponent: () =>
          import(
            './features/alumni-referrals/alumni-referral-list.component'
          ).then((m) => m.AlumniReferralListComponent),
      },
      {
        path: 'boomerang-applications',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/boomerang-applications/boomerang-application-list.component'
          ).then((m) => m.BoomerangApplicationListComponent),
      },
      {
        path: 'rehire-eligibilities',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/rehire-eligibilities/rehire-eligibility-list.component'
          ).then((m) => m.RehireEligibilityListComponent),
      },
      {
        path: 'alumni-reports',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'] },
        loadComponent: () =>
          import(
            './features/alumni-reports/alumni-report-list.component'
          ).then((m) => m.AlumniReportListComponent),
      },
      { path: '**', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
