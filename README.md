# TicketFlow

An IT support ticket management system: employees raise tickets, agents work
them, and every new ticket is triaged by a pluggable AI service that
suggests a priority and writes a one-line summary.

Think of this repo as a codebase you've just inherited on your first day at
a new job. It's a real, working system - not a toy - but it has known rough
edges on purpose. **See `BACKLOG.md` for the list of bugs to fix and
features to add.** That file is your onboarding task list.

## Stack

- Java 21, Spring Boot 3.3
- Spring Data JPA + PostgreSQL (Flyway migrations), H2 for tests
- Spring Security 6 with stateless JWT auth, role-based method security
- Pluggable AI layer (`TicketAiService`): a deterministic offline `mock`
  provider by default, with a real `openai` provider ready to switch on
- JUnit 5, Mockito, AssertJ, `@DataJpaTest`, `@WebMvcTest`

## Domain model

```
Department 1---* Employee 1---* Ticket *---1 Department
                                  |
                                  1---* Comment
                                  |
                          (assignedTo: Employee, nullable)
```

- **Employee** has a `Role`: `ADMIN`, `AGENT`, or `USER`.
- **Ticket** has a `TicketStatus` (`OPEN`, `IN_PROGRESS`, `RESOLVED`,
  `CLOSED`) and a `TicketPriority` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`).
- On creation, a ticket is sent to `TicketAiService`, which returns a
  suggested priority and a short summary - both get stored on the ticket.

## Running it locally

You already have PostgreSQL running natively. Create the database and user
(adjust names/password to taste, then set the matching env vars below):

```sql
CREATE DATABASE ticketflow_db;
CREATE USER ticketflow_user WITH PASSWORD 'ticketflow_pass';
GRANT ALL PRIVILEGES ON DATABASE ticketflow_db TO ticketflow_user;
```

Flyway creates the schema and seeds demo data automatically on startup -
you don't need to run any SQL by hand beyond creating the empty database.

```bash
export DB_NAME=ticketflow_db
export DB_USER=ticketflow_user
export DB_PASSWORD=ticketflow_pass
export JWT_SECRET=change-me-to-something-long-and-random

mvn spring-boot:run
```

The app starts on `http://localhost:8080`.

### Demo login

Every seeded employee uses the password `password123`:

| Email                          | Role  | Department   |
|---------------------------------|-------|--------------|
| ada.admin@ticketflow.dev        | ADMIN | IT Support   |
| sam.support@ticketflow.dev      | AGENT | IT Support   |
| priya.patel@ticketflow.dev      | AGENT | Engineering  |
| uche.user@ticketflow.dev        | USER  | Engineering  |

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada.admin@ticketflow.dev","password":"password123"}'
```

Copy the returned `token` and send it as `Authorization: Bearer <token>` on
every other request.

## Running the tests

```bash
mvn test
```

Tests run against H2 (`src/test/resources/application-test.yml`), so no
local Postgres is needed to run the suite.

## API overview

| Method | Path                              | Notes                              |
|--------|------------------------------------|-------------------------------------|
| POST   | `/api/auth/login`                  | Public. Returns a JWT.              |
| GET    | `/api/departments`                 | Any authenticated user.             |
| POST   | `/api/departments`                 | ADMIN only.                         |
| DELETE | `/api/departments/{id}`            | ADMIN only. **See BACKLOG #1.**     |
| GET    | `/api/employees`                   | ADMIN/AGENT only.                   |
| POST   | `/api/employees`                   | ADMIN only.                         |
| POST   | `/api/tickets`                     | Runs AI triage synchronously.       |
| GET    | `/api/tickets`                     | Paginated. **See BACKLOG #5.**      |
| GET    | `/api/tickets?status=OPEN`         | Paginated, filtered by status.      |
| GET    | `/api/tickets/{id}`                | Full detail incl. comment thread.   |
| PATCH  | `/api/tickets/{id}`                | Partial update. **See BACKLOG #2/3.**|
| DELETE | `/api/tickets/{id}`                | ADMIN/AGENT only.                   |
| POST   | `/api/tickets/{id}/comments`       | Adds a comment to the thread.       |

## Switching on the real AI provider

By default `ticketflow.ai.provider=mock` (see `application.yml`), which uses
keyword rules in `MockTicketAiService` - no API key, no network calls,
fully deterministic (and what the tests run against).

To use `OpenAiTicketAiService` instead:

```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...
```

That implementation is a starting point, not a finished feature - see
BACKLOG.md item #6 for what's missing before it's production-ready.

## Project layout

```
domain/            entities + enums
repository/         Spring Data JPA repositories
dto/request|response/  request/response records, mapped via fromEntity()
service/            business logic; service/ai holds the pluggable AI layer
controller/         REST controllers
security/           JWT filter, UserDetails, UserDetailsService
config/             SecurityConfig, AiConfig
exception/          domain exceptions + GlobalExceptionHandler
```
