/** Matches the backend ApiErrorResponse shape returned by GlobalExceptionHandler. */
export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors?: { [field: string]: string };
}
