# 🍁 ATIE — Autonomous Tax Intelligence Engine

> **AI constructs. Human supervises. System files.**

[![CI](https://github.com/tundelawal/wealthsimple-atie/actions/workflows/ci.yml/badge.svg)](https://github.com/tundelawal/wealthsimple-atie/actions/workflows/ci.yml)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-5.9-3178C6?logo=typescript&logoColor=white)
![Tests](https://img.shields.io/badge/tests-35%20passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

ATIE is an end-to-end Canadian personal income tax workflow engine that pairs deterministic financial calculations with GPT-4o–powered document intelligence. Upload your T-slips, reconcile against CRA data, optimise your RRSP, assess your audit risk, and route anomalies to a human reviewer — all through a single, auditable system.

Every AI decision is logged with its confidence score and prompt summary. No calculation is delegated to the LLM. No LLM response reaches the user without schema validation.

---

## ✨ Features

| # | Module | What it does |
|---|--------|--------------|
| 1 | **Document Upload** | Upload a PDF (T4, T5, T2202, RRSP). GPT-4o extracts tax fields with per-field confidence scores. |
| 2 | **CRA Import** | Pulls Auto-fill My Return (AFR) data from CRA — mock mode in development, live-ready when certified. |
| 3 | **Reconciliation** | Compares uploaded values against CRA data field-by-field. AI explains mismatches. Discrepancies are severity-rated and routed to human review. |
| 4 | **RRSP Optimisation** | Simulate additional RRSP contributions. Java calculates the exact tax savings; GPT-4o narrates the liquidity tradeoff. |
| 5 | **Risk Assessment** | Scores your return 0 – 100 for CRA reassessment risk. Java weighs the risk drivers; AI explains what to watch. |
| 6 | **Provincial Policy** | Province- and territory-specific credits and deductions analysis. Covers all 13 provinces and territories. |
| 7 | **Review & Audit** | Human review queue for flagged items. Full AI decision audit trail per profile. |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  React 19 + TypeScript 5.9  (ui/)                               │
│  TanStack Query · React Router DOM 7 · react-hot-toast          │
└───────────────────────────┬─────────────────────────────────────┘
                            │ REST  /api/v1/*
┌───────────────────────────▼─────────────────────────────────────┐
│  Spring Boot 3.4.3  (port 8080)                                 │
│                                                                 │
│  Controller  →  Service layer  →  Spring Data JPA               │
│                      │                  │                       │
│              ┌───────┴───────┐    PostgreSQL 16                 │
│              │               │                                  │
│     Deterministic       AI Agents  (Spring AI 1.0.0-M6)        │
│     Java logic          GPT-4o via OpenAI API                   │
│     TaxBracketCalculator                                        │
│     RiskScoring         DocumentExtractionAgent                 │
│     Eligibility rules   ReconciliationExplanationAgent          │
│                         OptimisationTradeoffAgent               │
│                         ProvincialPolicyAgent                   │
└─────────────────────────────────────────────────────────────────┘
```

### AI Boundary

The dividing line between deterministic code and the LLM is explicit and enforced:

| Layer | Responsibility |
|-------|----------------|
| **Java (deterministic)** | Tax bracket calculations, risk score weighting, discrepancy thresholds, eligibility rules, all numeric outputs |
| **GPT-4o (language)** | PDF field extraction, discrepancy explanations, RRSP tradeoff narrative, provincial policy recommendations |

Every LLM response is validated against a JSON schema before it reaches the service layer. Every LLM call is persisted in `AiDecisionLog` with its confidence score and prompt summary.

---

## 🚀 Quick Start

### Prerequisites

- **Java 21** (`java -version`)
- **Docker Desktop** (for PostgreSQL)
- **Node.js LTS** + npm
- **OpenAI API key** (GPT-4o access)

### 1 — Clone & configure

```bash
git clone https://github.com/tundelawal/wealthsimple-atie.git
cd wealthsimple-atie
```

Create a `.env` file in the project root (never committed):

```env
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password
POSTGRES_PASSWORD=your_db_password
OPENAI_API_KEY=sk-...
```

### 2 — Start PostgreSQL

```bash
docker compose up -d
```

### 3 — Start the backend

```bash
export $(grep -v '^#' .env | xargs) && ./mvnw spring-boot:run
```

Backend is live at **`http://localhost:8080`**  
Interactive API docs: **`http://localhost:8080/swagger-ui.html`**

### 4 — Start the frontend

```bash
cd ui
npm install
npm run dev
```

Frontend is live at **`http://localhost:5173`**

---

## 🔌 API Reference

All endpoints are prefixed `/api/v1/`. Full interactive documentation is available at [`/swagger-ui.html`](http://localhost:8080/swagger-ui.html).

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/documents/extract` | Upload a PDF and extract tax fields via AI |
| `POST` | `/api/v1/cra/import` | Import CRA Auto-fill My Return data |
| `POST` | `/api/v1/reconciliation/run` | Reconcile uploaded values against CRA data |
| `POST` | `/api/v1/optimization/rrsp` | Simulate RRSP contributions and compute tax savings |
| `POST` | `/api/v1/risk/assess` | Score the return for CRA reassessment risk |
| `POST` | `/api/v1/provincial/analyse` | Provincial credits and policy recommendations |
| `GET`  | `/api/v1/review/pending` | All items requiring human review |
| `GET`  | `/api/v1/review/pending/{profileId}` | Pending items for a specific profile |
| `GET`  | `/api/v1/review/audit/{profileId}` | Full AI decision audit trail for a profile |

---

## 🧪 Testing

```bash
# Backend — 35 unit tests
./mvnw test

# Frontend — TypeScript type check
cd ui && npx tsc --noEmit

# Frontend — production build (definitive bundle check)
cd ui && npm run build
```

Tests run against an **H2 in-memory database** — no Docker dependency.

### Test coverage

| Suite | What's covered |
|-------|----------------|
| `TaxBracketCalculatorTest` | Federal bracket accuracy, monotonicity, marginal rate boundaries |
| `ReconciliationServiceTest` | Discrepancy detection, threshold logic, severity classification |
| `OptimizationServiceTest` | Savings computation, floor-at-zero, AI explanation presence |
| `RiskAssessmentServiceTest` | Score weighting, risk driver accumulation |
| `WealthsimpleAtieApplicationTests` | Spring application context loads |

---

## 📁 Project Structure

```
wealthsimple-atie/
├── src/main/java/com/devlawal/wealthsimpleatie/
│   ├── ai/                  # Spring AI agents (GPT-4o calls only)
│   ├── config/              # AtieProperties, CORS, OpenAI config
│   ├── controller/          # REST controllers — one per domain
│   ├── dto/                 # Immutable Java records (request + response)
│   ├── model/               # JPA entities (TaxProfile, AiDecisionLog, …)
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic + AI orchestration
│   ├── exception/           # GlobalExceptionHandler, RFC 7807 ProblemDetail
│   └── util/                # TaxBracketCalculator, JsonResponseValidator
│
├── src/test/                # JUnit 5 — 35 tests, H2 in-memory DB
│
├── ui/                      # React 19 + TypeScript 5.9 frontend
│   └── src/
│       ├── api/             # Axios client, typed endpoints, DTO types
│       ├── components/      # Layout, ErrorBoundary, Skeleton, InlineMessage
│       └── pages/           # One page component per workflow module
│
├── docker-compose.yml       # PostgreSQL 16 (host port 5333)
├── pom.xml
└── .github/workflows/ci.yml # CI: backend tests + frontend build
```

---

## 🛡 Design Principles

1. **Constructor injection only** — no `@Autowired` field injection
2. **Records for DTOs** — immutable, compact, serialization-friendly
3. **Builders for domain models** — explicit construction, no setters
4. **LLM for language, Java for math** — strict AI boundary, always enforced
5. **JSON schema enforcement** — all LLM responses validated before use
6. **RFC 7807 error responses** — consistent `ProblemDetail` across all errors
7. **Configurable thresholds** — all tuning values in `application.yml`, no magic numbers

---

## 📋 2025 Federal Tax Brackets

| Income Range | Marginal Rate |
|---|---|
| $0 – $57,375 | 15% |
| $57,375 – $114,750 | 20.5% |
| $114,750 – $158,519 | 26% |
| $158,519 – $220,000 | 29% |
| $220,000+ | 33% |

---

## 🗺 Roadmap

- [ ] Flyway versioned migrations (replace `ddl-auto=update`)
- [ ] Vitest + React Testing Library for frontend pages
- [ ] JWT authentication and session management
- [ ] Multi-stage Dockerfile — `npm run build` → Nginx for frontend
- [ ] Live CRA AFR integration (post NETFILE transmitter certification)
- [ ] PDF preview alongside extracted fields

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 21, Spring Boot 3.4.3 |
| AI | Spring AI 1.0.0-M6, OpenAI GPT-4o |
| Persistence | Spring Data JPA, PostgreSQL 16, Hibernate |
| PDF | Apache PDFBox 3.0.3 |
| API Docs | springdoc-openapi (Swagger UI) |
| Frontend | React 19, TypeScript 5.9, Vite 7.3.1 |
| Routing | React Router DOM 7 |
| Data fetching | TanStack Query v5, Axios |
| CI | GitHub Actions |

---

<div align="center">
  <sub>© 2026 Tundelawal. All rights reserved.</sub>
  <br/>
  <sub>🇨🇦 Built in Canada</sub>
</div>
