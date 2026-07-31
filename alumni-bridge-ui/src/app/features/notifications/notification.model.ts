import {
  NotificationCategory,
  NotificationStatus,
} from '../../core/models/enums';

export interface NotificationResponse {
  notificationId: number;
  employeeId: string;
  userName?: string;
  message: string;
  category: NotificationCategory;
  status: NotificationStatus;
  createdDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateNotificationRequest {
  employeeId: string;
  userName?: string;
  userEmail?: string;
  message: string;
  category: NotificationCategory;
}

export interface UpdateNotificationRequest {
  message?: string;
  category?: NotificationCategory;
  status?: NotificationStatus;
}
