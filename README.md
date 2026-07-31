# AI-Powered Finance Tracker -- Backend (Spring Boot)

Java 17 + Spring Boot 3 REST API implementing the AI-Powered Finance Tracker:
JWT auth, transaction CRUD with filters/sort/pagination, monthly + category
budgets, an AI insights endpoint (OpenAI `gpt-4o-mini` with a deterministic
heuristic fallback), a local spending prediction, and a monthly report with
PDF export.

## Run it

```bash
cd backend
mvn spring-boot:run
```

The app starts on **http://localhost:5000** using an in-memory H2 database --
no setup required. Data resets on restart. To persist data, switch to MySQL:
uncomment the MySQL block and comment out the H2 block in
`src/main/resources/application.properties`, then create the dependency-listed
database.

The H2 console (handy for poking at data while developing) is at
`http://localhost:5000/h2-console` -- JDBC URL `jdbc:h2:mem:financetracker`,
user `sa`, empty password.

## Configuration

Everything lives in `src/main/resources/application.properties`:

- `app.jwt.secret` / `app.jwt.expiration-ms` -- change the secret before any
  real deployment.
- `app.cors.allowed-origins` -- defaults to the Vite dev server
  (`http://localhost:5173`).
- `app.openai.api-key` -- optional. Leave blank to use the built-in heuristic
  insight engine (`FinanceAnalyzerService`). Set a real key to have
  `/api/ai/insights` call OpenAI's `gpt-4o-mini` instead -- if that call ever
  fails, the response silently falls back to the heuristic insights so the
  feature never breaks.

## Architecture notes

- `FinanceAnalyzerService` is the single deterministic source of truth for all
  totals, category breakdowns, monthly trend, prediction, and rule-based
  insights -- reused by the dashboard, reports, account stats, and as the
  grounding data for the AI prompt.
- `AIService` only ever sends aggregate numbers to OpenAI, never individual
  transactions.
- Every query in `TransactionRepository` / `BudgetRepository` is scoped by the
  authenticated user's id, enforced again at the service layer, so one user
  can never read or modify another's data.
- `JwtAuthFilter` verifies `Authorization: Bearer <token>` on every request
  except `/api/auth/signup`, `/api/auth/login`, and `/health`.

## API surface

Same contract as the reference spec: `/api/auth/*`, `/api/transactions`,
`/api/budget`, `/api/dashboard/summary`, `/api/ai/insights`, `/api/ai/predict`,
`/api/reports/monthly?year=&month=&format=json|pdf`, `/health`.
Every JSON response is shaped `{ success, message, data }`.
