export const environment = {
  production: false,
  // All requests go through the API Gateway (see api-gateway/application.yml).
  // The gateway routes /api/audit-logs to audit-service, /api/notifications to
  // notification-service and everything else to the alumni-bridge monolith.
  apiBaseUrl: 'http://localhost:8081/api',
};
