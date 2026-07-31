# Alumni Bridge UI

Angular front end for the Alumni Bridge offboarding & alumni-engagement platform.
It talks **only to the API Gateway** (`http://localhost:8081`), which routes to the
monolith, the audit-service and the notification-service.

## Tech stack

- Angular 18 (NgModules, lazy-loaded feature modules)
- Angular Material + SCSS
- Reactive Forms, RxJS, HttpClient
- JWT bearer authentication via an HTTP interceptor

## Prerequisites

- Node.js 18/20/22 (works on newer, Angular prints an "odd version" notice only)
- The backend running locally:
  - Eureka server on `8761`
  - API Gateway on `8081`
  - Alumni Bridge monolith on `8085`
  - Audit service on `8082`, Notification service on `8083`
  - MySQL as configured in the monolith `application.properties`

## Run

```bash
cd alumni-bridge-ui
npm install
npm start            # ng serve on http://localhost:4200
```

Build a production bundle:

```bash
npm run build        # outputs to dist/alumni-bridge-ui
```

The gateway already allows the `http://localhost:4200` origin (see
`api-gateway/src/main/resources/application.yml`), so no proxy is required.

## Configuration

API base URL lives in `src/environments/environment.ts`
(`apiBaseUrl: 'http://localhost:8081/api'`). The production file uses a relative
`/api` so the app can be served behind the same host as the gateway.

## Authentication flow

1. `POST /api/auth/login` returns the JWT + user profile (`AuthResponse`).
2. The token + profile are stored in `localStorage`; `AuthInterceptor` adds
   `Authorization: Bearer <token>` to every request.
3. `authGuard` protects the shell, `roleGuard` restricts each route to the roles
   the backend actually permits. A `401` clears the session and redirects to login.
4. `GET /api/auth/me` is available via `AuthService.loadCurrentUser()`.

## Modules & backend API mapping

Roles shown are the roles the backend `@PreAuthorize` allows for the list route.

| Screen (route) | Backend base path | Roles |
|---|---|---|
| Login (`/login`) | `/api/auth/login`, `/api/auth/me` | public / authenticated |
| Dashboard (`/dashboard`) | — (role-based shortcuts) | all |
| User Management (`/users`) | `/api/users` | ADMIN, HRBP |
| Notifications (`/notifications`) | `/api/notifications` (notification-service) | ADMIN, HRBP, ALUMNI_RELATIONS, TALENT_ACQUISITION |
| Audit Logs (`/audit-logs`) | `/api/audit-logs` (audit-service) | ADMIN |
| Admin Configuration (`/admin-configs`) | `/api/admin-configs` | ADMIN |
| Offboarding Cases (`/offboarding-cases`) | `/api/offboarding-cases` | ADMIN, HRBP |
| Offboarding Tasks (`/offboarding-tasks`) | `/api/offboarding-tasks` | ADMIN, HRBP, IT_OPS |
| Exit Interviews (`/exit-interviews`) | `/api/exit-interviews` | ADMIN, HRBP |
| Exit Surveys (`/exit-surveys`) | `/api/exit-surveys` | ADMIN, HRBP |
| Clearance Certificates (`/clearance-certificates`) | `/api/clearance-certificates` | ADMIN, HRBP |
| Knowledge Handovers (`/knowledge-handovers`) | `/api/knowledge-handovers` | ADMIN, HRBP |
| Handover Documents (`/handover-documents`) | `/api/handover-documents` | ADMIN, HRBP |
| Alumni Profiles (`/alumni-profiles`) | `/api/alumni-profiles` | ADMIN, HRBP, ALUMNI_RELATIONS, TALENT_ACQUISITION |
| Alumni Events (`/alumni-events`) | `/api/alumni-events` | ADMIN, ALUMNI_RELATIONS, EMPLOYEE, TALENT_ACQUISITION |
| Referrals (`/alumni-referrals`) | `/api/alumni-referrals` | ADMIN, TALENT_ACQUISITION, ALUMNI_RELATIONS |
| Boomerang Applications (`/boomerang-applications`) | `/api/boomerang-applications` | ADMIN, HRBP, TALENT_ACQUISITION |
| Rehire Eligibility (`/rehire-eligibilities`) | `/api/rehire-eligibilities` | ADMIN, HRBP, TALENT_ACQUISITION |
| Alumni Reports (`/alumni-reports`) | `/api/alumni-reports` | ADMIN, HRBP, ALUMNI_RELATIONS, TALENT_ACQUISITION |

### Notes on backend constraints honoured by the UI

- No backend endpoint is paginated (all list endpoints return plain arrays), so
  tables paginate/sort/filter on the client via `MatTableDataSource`.
- `Offboarding Tasks` and `Handover Documents` have **no "list all" endpoint** — the
  UI loads them by case / assignee (tasks) or by status / type / handover (documents),
  matching the only endpoints the backend exposes.
- Delete actions are only shown to `ADMIN`, mirroring the `@PreAuthorize` on every
  `DELETE` endpoint.
- Enum dropdowns are generated from values mirrored 1:1 from the backend Java enums
  (`src/app/core/models/enums.ts`).

## Project structure

```
src/app
├── core            # guards, interceptors, singleton services, models, enums
├── shared          # MaterialModule + reusable components (page-header, dialogs, pipe)
├── layout          # authenticated shell (toolbar + role-aware sidenav)
└── features        # one lazy-loaded module per business area
    ├── auth
    ├── dashboard
    ├── users
    ├── notifications
    ├── audit
    ├── admin-config
    ├── offboarding-cases / offboarding-tasks
    ├── exit-interviews / exit-surveys / clearance
    ├── knowledge-handovers / handover-documents
    └── alumni-profiles / alumni-events / alumni-referrals
        / boomerang-applications / rehire-eligibilities / alumni-reports
```
