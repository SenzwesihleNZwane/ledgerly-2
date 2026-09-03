# Ledgerly

A group expense splitting app that tells you the **minimum number of payments**
needed to settle up not just who paid what.

![Java](https://img.shields.io/badge/Java-17-b1443a) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-2f6f4f) ![JavaScript](https://img.shields.io/badge/JavaScript-Vanilla-9c7a2e)

## The problem

Splitting shared costs — a trip, a shared house, a group gift — is where
friendships get awkward. People forget who paid for what, the maths gets
messy fast, and you often end up with far more payments than necessary to
settle a group ("I owe Alice, Alice owes Bob, Bob owes me..." — three
payments that could have been one).

Ledgerly fixes this two ways:
1. It tracks who paid what, and how it's split, so nobody has to remember.
2. It runs a **debt-simplification algorithm** over the group's balances
   and outputs the smallest set of transactions that settles everyone up.

## How the settlement algorithm works

After every expense, each member has a **net balance**: what they paid,
minus what they owe across all expenses. Some members are net creditors
(positive balance, owed money), some are net debtors (negative balance,
owe money).

Instead of settling every pairwise debt individually, `BalanceService`
repeatedly finds the biggest creditor and the biggest debtor remaining,
and settles the smaller of the two amounts between them directly. This
greedy approach — the same technique real expense-splitting apps use —
collapses what could be many small IOUs into a short, practical payment
list.

```
Alice paid 90 for dinner, split evenly 3 ways (Alice, Bob, Carol)
  -> Alice net: +60   Bob net: -30   Carol net: -30

Naive approach: 2+ payments per pair, more transactions than needed
Ledgerly:       Bob -> Alice: 30
                Carol -> Alice: 30
```

This logic is covered by unit tests in `BalanceServiceTest`.

## Tech stack

**Backend:** Java 17, Spring Boot 3 (Web, Data JPA, Security), H2 (file-based,
zero setup), JWT authentication, Maven.

**Frontend:** Vanilla HTML/CSS/JavaScript — no framework, no build step, so
it runs by just opening `index.html` (or via a simple dev server) once the
API is up.

## Project structure

```
ledgerly/
├── backend/                  Spring Boot API (Maven project — open the repo root in VS Code)
│   └── src/main/java/com/ledgerly/
│       ├── model/            JPA entities: User, Group, Expense, ExpenseSplit
│       ├── repository/       Spring Data repositories
│       ├── dto/               Request/response objects
│       ├── security/          JWT filter + util
│       ├── config/            Spring Security config
│       ├── service/            Business logic - AuthService, GroupService,
│       │                       ExpenseService, BalanceService (the algorithm)
│       └── controller/         REST endpoints
└── frontend/                  Static HTML/CSS/JS client
    ├── index.html
    ├── css/style.css
    └── js/{api.js, app.js}
```

## Running it locally (VS Code)

Requires Java 17+ and Maven installed on your machine (VS Code doesn't
bundle either, unlike NetBeans — see **Prerequisites** below).

1. Open the `ledgerly` folder (the repo root, not `backend` on its own) in
   VS Code. On first open it will prompt you to install the recommended
   extensions from `.vscode/extensions.json` — accept that prompt, or
   install them manually from the Extensions panel:
   - **Extension Pack for Java** (`vscjava.vscode-java-pack`) — Java
     language support, debugger, Maven, project explorer.
   - **Spring Boot Extension Pack** (`vmware.vscode-spring-boot` +
     `vscjava.vscode-spring-boot-dashboard`) — Spring Boot run/debug
     support, the Spring Dashboard view, `application.properties`
     autocomplete.
   - **Live Server** (`ritwickdey.liveserver`) — serves the frontend
     without a build step.
2. Wait for the Java extension to finish importing the Maven project
   (watch the bottom status bar — it'll say "Importing Maven projects").

### Backend

Two ways to run it, both work with no NetBeans involved:

- **Run/Debug button:** open `LedgerlyApplication.java`, click the ▶ Run
  (or 🐞 Debug) codelens above `public static void main`. A preconfigured
  launch config (`.vscode/launch.json`) is already set up for this — it
  sets the working directory to `backend` so the H2 database file lands
  in the right place.
- **Spring Boot Dashboard:** open the Spring Boot Dashboard view in the
  sidebar, find `ledgerly-backend` under Local Apps, click ▶.
- **Terminal**, from VS Code's integrated terminal:
  ```bash
  cd backend
  mvn spring-boot:run
  ```

The API starts on `http://localhost:8080`. It uses an embedded H2 database
that persists to `backend/data/` — no separate database install needed.
Swap it for Postgres/MySQL later by editing `application.properties`.

### Frontend

No build step required. Right-click `frontend/index.html` in the VS Code
explorer and choose "Open with Live Server", or click "Go Live" in the
status bar. Or just open the file directly in a browser once the backend
is running.

If your backend runs anywhere other than `localhost:8080`, update
`API_BASE` at the top of `frontend/js/api.js`.

### Prerequisites

VS Code needs these installed separately (NetBeans bundles a JDK and
Maven, so this step wasn't needed there):
- **JDK 17+** — VS Code's Java extension can also download one for you
  on first launch if it doesn't find one.
- **Maven** — only required for the terminal option above; the Maven
  extension can use its own bundled Maven if none is found on your PATH.

### Try it out

1. Register two accounts (e.g. `alice@test.com` and `bob@test.com`).
2. Log in as Alice, create a group, and add Bob by email.
3. Log an expense paid by Alice, split equally between both.
4. Check the Balances panel — it'll show Bob owes Alice, with the
   settlement plan already worked out.

## API overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Log in, returns a JWT |
| POST | `/api/groups` | Create a group |
| GET | `/api/groups` | List your groups |
| POST | `/api/groups/{id}/members` | Add a member by email |
| POST | `/api/groups/{id}/expenses` | Log an expense (EQUAL or EXACT split) |
| GET | `/api/groups/{id}/expenses` | List a group's expenses |
| GET | `/api/groups/{id}/balances` | Net balances + simplified settlement plan |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

## What I'd add next

- Percentage-based splits, in addition to equal/exact
- Mark settlements as "paid" and track settlement history
- Expense categories and per-category totals
- Deploy: backend to Render/Railway, frontend to Netlify/Vercel
- Integration tests with Testcontainers + a real Postgres instance

## Why this project

I built this to practice full-stack development end to end: relational
data modeling, authentication, a REST API with real business logic (not
just CRUD), and a frontend that consumes it — plus one genuinely
interesting algorithmic problem (the debt simplification) worth testing
and being able to explain in an interview.
