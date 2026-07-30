# AI-Powered Finance Tracker

Full-stack rebuild of the reference spec using:

- **Backend:** Java 17 + Spring Boot 3 (JWT auth, JPA/H2 or MySQL, OpenAI-backed AI insights with a deterministic heuristic fallback, PDF report export)
- **Frontend:** React + Redux Toolkit + plain HTML/CSS (no Tailwind) + Recharts for charts
- **AI:** OpenAI `gpt-4o-mini` for natural-language insights, always backed by a local rules engine so the feature never breaks

```
finance-tracker/
├── backend/     Spring Boot API (see backend/README.md)
└── frontend/    React + Redux app (see frontend/README.md)
```

## Quick start

**1. Backend** (http://localhost:5000)
```bash
cd backend
mvn spring-boot:run
```
Runs instantly against an in-memory H2 database -- no setup needed.

**2. Frontend** (http://localhost:5173)
```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173, sign up, and start logging transactions. The
Vite dev server proxies `/api` calls to the backend automatically.

## What's implemented

- Signup / login with JWT, bcrypt-hashed passwords
- Transaction CRUD with filters (category, type, search), sorting, and pagination
- Monthly budget + per-category limits with progress bars and an overspend alert
- Dashboard: summary tiles, monthly trend line chart, category pie chart, income vs expense bar chart, recent activity
- AI Insights page: natural-language insights (OpenAI when configured, heuristic engine otherwise) + a spending prediction with a confidence score and budget-risk flag
- Reports page: pick a month/year, view a summary + category chart + transaction list, and export a PDF

## Design

The frontend uses a distinct "ledger book" visual identity -- warm paper
surfaces, deep ink text, brass for income, rust for expenses, and a
rotated ink-stamp mark as the signature element (used for the logo and the
AI-insight provider badge) -- rather than a generic dashboard template.
See `frontend/src/styles/global.css` for the full token system.

## Notes for extending this

- Swap MySQL in by editing `backend/src/main/resources/application.properties`.
- Add a real OpenAI key the same way -- one property, no code changes.
- Every backend query is scoped to the authenticated user's id at both the
  repository and service layer, so this is safe to extend with more
  endpoints without accidentally leaking cross-user data.
