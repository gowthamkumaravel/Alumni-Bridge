import { UserRole } from '../core/models/enums';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: UserRole[];
}

export interface NavSection {
  heading: string;
  items: NavItem[];
}

/**
 * Sidebar configuration. Each item lists the roles the backend actually allows
 * for that module's list endpoint, so the menu only shows reachable screens.
 */
export const NAV_SECTIONS: NavSection[] = [
  {
    heading: 'General',
    items: [
      {
        label: 'Dashboard',
        icon: 'dashboard',
        route: '/dashboard',
        roles: [
          'EMPLOYEE',
          'HRBP',
          'IT_OPS',
          'ALUMNI_RELATIONS',
          'TALENT_ACQUISITION',
          'ADMIN',
        ],
      },
    ],
  },
  {
    heading: 'Administration',
    items: [
      {
        label: 'User Management',
        icon: 'group',
        route: '/users',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Audit Logs',
        icon: 'fact_check',
        route: '/audit-logs',
        roles: ['ADMIN'],
      },
      {
        label: 'Notifications',
        icon: 'notifications',
        route: '/notifications',
        roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'],
      },
    ],
  },
  {
    heading: 'Offboarding',
    items: [
      {
        label: 'Offboarding Cases',
        icon: 'work_off',
        route: '/offboarding-cases',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Offboarding Tasks',
        icon: 'checklist',
        route: '/offboarding-tasks',
        roles: ['ADMIN', 'HRBP', 'IT_OPS'],
      },
      {
        label: 'Exit Interviews',
        icon: 'record_voice_over',
        route: '/exit-interviews',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Exit Surveys',
        icon: 'poll',
        route: '/exit-surveys',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Clearance Certificates',
        icon: 'verified',
        route: '/clearance-certificates',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Knowledge Handovers',
        icon: 'school',
        route: '/knowledge-handovers',
        roles: ['ADMIN', 'HRBP'],
      },
      {
        label: 'Handover Documents',
        icon: 'description',
        route: '/handover-documents',
        roles: ['ADMIN', 'HRBP'],
      },
    ],
  },
  {
    heading: 'Alumni',
    items: [
      {
        label: 'Alumni Profiles',
        icon: 'badge',
        route: '/alumni-profiles',
        roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'],
      },
      {
        label: 'Alumni Events',
        icon: 'event',
        route: '/alumni-events',
        roles: ['ADMIN', 'ALUMNI_RELATIONS', 'EMPLOYEE', 'TALENT_ACQUISITION'],
      },
      {
        label: 'Referrals',
        icon: 'connect_without_contact',
        route: '/alumni-referrals',
        roles: ['ADMIN', 'TALENT_ACQUISITION', 'ALUMNI_RELATIONS'],
      },
      {
        label: 'Boomerang Applications',
        icon: 'replay',
        route: '/boomerang-applications',
        roles: ['ADMIN', 'HRBP', 'TALENT_ACQUISITION'],
      },
      {
        label: 'Rehire Eligibility',
        icon: 'how_to_reg',
        route: '/rehire-eligibilities',
        roles: ['ADMIN', 'HRBP', 'TALENT_ACQUISITION'],
      },
      {
        label: 'Alumni Reports',
        icon: 'insights',
        route: '/alumni-reports',
        roles: ['ADMIN', 'HRBP', 'ALUMNI_RELATIONS', 'TALENT_ACQUISITION'],
      },
    ],
  },
];
