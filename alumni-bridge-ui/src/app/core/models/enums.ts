/**
 * Enum values mirrored 1:1 from the backend Java enums.
 * Each is declared as a readonly array (for dropdowns) plus a derived union type.
 */

export const USER_ROLES = [
  'EMPLOYEE',
  'HRBP',
  'IT_OPS',
  'ALUMNI_RELATIONS',
  'TALENT_ACQUISITION',
  'ADMIN',
] as const;
export type UserRole = (typeof USER_ROLES)[number];

export const ACCOUNT_TYPES = ['ACTIVE', 'OFFBOARDING', 'ALUMNI'] as const;
export type AccountType = (typeof ACCOUNT_TYPES)[number];

export const USER_STATUSES = [
  'ACTIVE',
  'OFFBOARDING',
  'ALUMNI',
  'INACTIVE',
] as const;
export type UserStatus = (typeof USER_STATUSES)[number];

export const ADMIN_CONFIG_TYPES = [
  'OFFBOARDING_CHECKLIST_TEMPLATE',
  'EXIT_SURVEY_TEMPLATE',
  'REHIRE_POLICY_RULE',
  'ALUMNI_PORTAL_SETTING',
  'ALUMNI_SEGMENT_TEMPLATE',
] as const;
export type AdminConfigType = (typeof ADMIN_CONFIG_TYPES)[number];

export const ALUMNI_STATUSES = ['ACTIVE', 'INACTIVE'] as const;
export type AlumniStatus = (typeof ALUMNI_STATUSES)[number];

export const ENGAGEMENT_TIERS = ['ENGAGED', 'PASSIVE', 'LAPSED'] as const;
export type EngagementTier = (typeof ENGAGEMENT_TIERS)[number];

export const EVENT_STATUSES = [
  'PLANNED',
  'PUBLISHED',
  'COMPLETED',
  'CANCELLED',
] as const;
export type EventStatus = (typeof EVENT_STATUSES)[number];

export const EVENT_TYPES = [
  'NETWORKING',
  'WEBINAR',
  'REUNION',
  'MENTOR_CIRCLE',
  'CAREER_FAIR',
] as const;
export type EventType = (typeof EVENT_TYPES)[number];

export const ALUMNI_REFERRAL_STATUSES = [
  'SUBMITTED',
  'CONTACTED',
  'PROGRESSED',
  'HIRED',
  'NOT_PROGRESSED',
] as const;
export type AlumniReferralStatus = (typeof ALUMNI_REFERRAL_STATUSES)[number];

export const REPORT_METRICS = [
  'ATTRITION_RATE',
  'AVG_TENURE',
  'EXIT_SURVEY_SCORE',
  'TOP_EXIT_REASONS',
  'ALUMNI_ENGAGEMENT_RATE',
  'BOOMERANG_HIRE_RATE',
  'REFERRAL_CONVERSION_RATE',
] as const;
export type ReportMetric = (typeof REPORT_METRICS)[number];

export const REPORT_SCOPES = [
  'DEPARTMENT',
  'GRADE',
  'DEPARTURE_TYPE',
  'PERIOD',
] as const;
export type ReportScope = (typeof REPORT_SCOPES)[number];

export const BOOMERANG_APPLICATION_STATUSES = [
  'APPLIED',
  'SHORTLISTED',
  'INTERVIEWED',
  'OFFERED',
  'JOINED',
  'DECLINED',
] as const;
export type BoomerangApplicationStatus =
  (typeof BOOMERANG_APPLICATION_STATUSES)[number];

export const CLEARANCE_STATUSES = [
  'CLEAR',
  'PENDING_ITEMS',
  'WITHHELD_DUE_TO_OUTSTANDING',
] as const;
export type ClearanceStatus = (typeof CLEARANCE_STATUSES)[number];

export const ELIGIBLE_FOR_REHIRE = ['YES', 'NO', 'CONDITIONAL'] as const;
export type EligibleForRehire = (typeof ELIGIBLE_FOR_REHIRE)[number];

export const REHIRE_ELIGIBILITY_STATUSES = [
  'ELIGIBLE',
  'INELIGIBLE',
  'UNDER_REVIEW',
] as const;
export type RehireEligibilityStatus =
  (typeof REHIRE_ELIGIBILITY_STATUSES)[number];

export const EXIT_INTERVIEW_STATUSES = [
  'SCHEDULED',
  'COMPLETED',
  'DECLINED',
  'NO_SHOW',
] as const;
export type ExitInterviewStatus = (typeof EXIT_INTERVIEW_STATUSES)[number];

export const INTERVIEW_FORMATS = ['IN_PERSON', 'VIRTUAL', 'SELF_SERVE'] as const;
export type InterviewFormat = (typeof INTERVIEW_FORMATS)[number];

export const OFFBOARDING_STATUSES = [
  'INITIATED',
  'IN_PROGRESS',
  'CLEARANCE_PENDING',
  'COMPLETED',
  'CANCELLED',
] as const;
export type OffboardingStatus = (typeof OFFBOARDING_STATUSES)[number];

export const DEPARTURE_TYPES = [
  'RESIGNATION',
  'RETIREMENT',
  'REDUNDANCY',
  'END_OF_CONTRACT',
  'TERMINATION',
] as const;
export type DepartureType = (typeof DEPARTURE_TYPES)[number];

export const TASK_CATEGORIES = [
  'IT',
  'HR',
  'FINANCE',
  'FACILITIES',
  'MANAGER',
  'LEGAL',
] as const;
export type TaskCategory = (typeof TASK_CATEGORIES)[number];

export const TASK_STATUSES = [
  'PENDING',
  'IN_PROGRESS',
  'COMPLETED',
  'OVERDUE',
  'WAIVED',
] as const;
export type TaskStatus = (typeof TASK_STATUSES)[number];

export const DOCUMENT_STATUSES = ['UPLOADED', 'REVIEWED_BY_SUCCESSOR'] as const;
export type DocumentStatus = (typeof DOCUMENT_STATUSES)[number];

export const DOCUMENT_TYPES = [
  'PROCESS_GUIDE',
  'PROJECT_HANDOVER_NOTE',
  'CONTACT_LIST',
  'ACCESS_CREDENTIALS',
  'TRAINING_NOTE',
] as const;
export type DocumentType = (typeof DOCUMENT_TYPES)[number];

export const HANDOVER_STATUSES = [
  'DRAFT',
  'IN_PROGRESS',
  'COMPLETED',
  'MANAGER_SIGNED',
] as const;
export type HandoverStatus = (typeof HANDOVER_STATUSES)[number];

export const NOTIFICATION_CATEGORIES = [
  'OFFBOARDING',
  'EXIT_INTERVIEW',
  'KNOWLEDGE_HANDOVER',
  'ALUMNI_EVENT',
  'REHIRE_OPPORTUNITY',
  'REFERRAL',
] as const;
export type NotificationCategory = (typeof NOTIFICATION_CATEGORIES)[number];

export const NOTIFICATION_STATUSES = ['UNREAD', 'READ', 'DISMISSED'] as const;
export type NotificationStatus = (typeof NOTIFICATION_STATUSES)[number];

/** Convert an ENUM_VALUE into a human friendly label (e.g. "Career Fair"). */
export function humanizeEnum(value: string | null | undefined): string {
  if (!value) {
    return '';
  }
  return value
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}
