
# Backlog

Your first-day task list. These are real, working issues planted in the
codebase on purpose - each one is called out in a code comment right where
it lives, so start there. Nothing here needs external context you don't
have; everything is inferable from the code and tests.

## Bugs

### 1. Deleting a department doesn't check for existing employees
`DepartmentService.delete()` deletes the row outright. If any `Employee`
still references that department, the FK constraint in `V1__init_schema.sql`
rejects it at the database level, which surfaces as a raw
`DataIntegrityViolationException` (mapped to a generic 500) instead of a
clean, explanatory 409. Decide the right behaviour (block the delete with a
clear message, or offer a reassignment step) and implement it.

### 2. Ticket assignment doesn't validate department membership
`TicketService.assignTicket()` (private, called from `update()`) assigns any
employee to any ticket, regardless of department. Confirm with the team
whether cross-department assignment should ever be allowed (some orgs want
generalist agents who float across departments) - then either enforce the
match with a `BusinessRuleException` or document why it's intentionally
open, and add a test either way.

### 3. AI fields go stale after an edit
`TicketService.update()` lets you change `description` without re-running
AI analysis or clearing the old `aiSummary` / `aiSuggestedPriority`. After an
edit, those fields describe the *original* ticket text, not the current one.
Decide: re-analyze on every description change (costs an API call each
time), or clear the fields and expose a "needs re-analysis" flag the UI can
show. There's no test covering this yet - write one that captures whichever
behaviour you land on.

### 4. Urgency sort returns least-urgent first
`TicketService.sortByUrgencyDescending()` is supposed to put the most urgent
ticket first but sorts using `TicketPriority`'s natural (ordinal) order
without reversing it, so `CRITICAL` tickets end up last. There's a test
(`TicketServiceTest#sortByUrgencyDescending_currentlyReturnsLeastUrgentFirst_dueToKnownBug`)
that documents the current (wrong) behaviour - fixing the method should make
that test fail, which is your cue to rewrite its assertions to the correct
order.

### 5. Ticket list endpoints have no default sort
`GET /api/tickets` and `GET /api/tickets?status=X` accept a `Pageable` but
nothing pins a default sort order, so results can drift between requests
depending on how Postgres executes the query. Add a sensible default (e.g.
`createdAt` descending) - probably at the `@PageableDefault` annotation on
the controller methods.

## Features to add

6. **Finish the OpenAI provider.** `OpenAiTicketAiService` calls the real
   API but has no retry/backoff, no timeout configuration, no tests, and a
   parsing path that assumes the model always returns valid JSON. Harden it,
   then write tests (you'll need to mock `RestClient` or extract an HTTP
   abstraction).
7. **Move AI triage off the request thread.** Ticket creation currently
   blocks on `TicketAiService.analyze()` synchronously. Fine for the mock
   provider; not fine once real network calls are involved. Consider
   `@Async` + a status field on the ticket ("analyzing" → "analyzed"), or a
   lightweight queue.
8. **Email notifications** when a ticket is assigned or commented on.
9. **Ticket attachments** (e.g. screenshots) - needs a storage decision
   (filesystem vs S3-compatible bucket) and a new entity/table.
10. **SLA timers** - flag tickets that have been open past a
    priority-based threshold (e.g. CRITICAL > 4 hours) for follow-up.
11. **Audit trail** - who changed what and when on a ticket (status changes,
    reassignments), separate from the comment thread.
12. **Dashboard/analytics endpoint** - counts by status/priority/department,
    average time-to-resolution.
13. **Docker Compose** for local Postgres, so contributors don't need a
    native install to run the app.
14. **CI pipeline** (GitHub Actions) running `mvn test` on every PR.
15. **Rate limiting** on `/api/auth/login` to slow down credential stuffing.
