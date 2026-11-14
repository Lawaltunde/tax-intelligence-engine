# Autonomous Tax Intelligence Engine (ATIE)

[![CI](https://github.com/Lawaltunde/tax-intelligence-engine/actions/workflows/ci.yml/badge.svg)](https://github.com/Lawaltunde/tax-intelligence-engine/actions/workflows/ci.yml)

An AI-native Canadian tax orchestration system built with **Java 21 + Spring Boot 3 + Spring AI**.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         REST API Layer                          │
│  DocumentController  ReconciliationController  OptimizationController  RiskController  │
└────────────────────────────┬────────────────────────────────────┘
                             │ delegates to
┌────────────────────────────▼────────────────────────────────────┐
│                        Service Layer                            │
│  DocumentExtractionService  CraImportService                    │
│  ReconciliationService  OptimizationService  RiskAssessmentService │
└──────────┬──────────────────────────────┬───────────────────────┘
           │ LLM calls only               │ deterministic logic
┌──────────▼───────────┐        ┌─────────▼──────────────────────┐
│     AI Agent Layer   │        │        Utility Layer           │
│  TaxSlipExtraction   │        │  TaxBracketCalculator          │
│  ReconciliationExpl. │        │  PdfTextExtractor              │
│  OptimizationExpl.   │        │  JsonResponseValidator         │
└──────────────────────┘        └────────────────────────────────┘
```

### AI Boundary Discipline

| Responsibility | Owner |
|---|---|
| Numeric tax calculations | ✅ Deterministic Java |
| Marginal bracket computation | ✅ Deterministic Java |
| Field-level numeric comparison | ✅ Deterministic Java |
| Risk score heuristics | ✅ Deterministic Java |
| PDF text extraction | ✅ PDFBox |
| Structured field extraction from document text | 🤖 Spring AI / GPT-4o |
| Discrepancy explanation | 🤖 Spring AI / GPT-4o |
| RRSP tradeoff explanation | 🤖 Spring AI / GPT-4o |

---

## Package Structure

```
com.devlawal.wealthsimpleatie
 ├── controller     — HTTP entry points (no business logic)
 ├── service        — Orchestration and business logic
 ├── ai             — LLM agents (extraction + explanation only)
 ├── model          — Immutable domain objects (builder pattern)
 ├── dto            — Records for API I/O and AI JSON schemas
 ├── util           — PdfTextExtractor, TaxBracketCalculator, JsonResponseValidator
 ├── config         — SpringAiConfig, AtieProperties, AppConfig
 └── exception      — Typed exceptions + RFC 7807 GlobalExceptionHandler
```

---

## Prerequisites

- Java 21
- Maven 3.9+
- OpenAI API key

---

## Configuration

Set your OpenAI API key as an environment variable:

```bash
export OPENAI_API_KEY=sk-...
```

Or override in `application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: sk-...
```

---

## Running

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

---

## API Endpoints

### 1. Extract Tax Fields from PDF

```bash
curl -X POST http://localhost:8080/api/v1/documents/extract \
  -F "file=@/path/to/T4.pdf" \
  -F "taxYear=2024"
```

**Response:**
```json
{
  "profileId": "a1b2c3d4-...",
  "taxYear": "2024",
  "employmentIncome": 52000.00,
  "rrspContribution": 5000.00,
  "capitalGains": 1200.00,
  "confidenceScores": {
    "employmentIncome": 0.94,
    "rrspContribution": 0.87,
    "capitalGains": 0.76
  }
}
```

---

### 2. Reconcile Against CRA Data

```bash
curl -X POST "http://localhost:8080/api/v1/reconciliation/run?sin=123456789&taxYear=2024" \
  -H "Content-Type: application/json" \
  -d '{
    "employmentIncome": 52000.00,
    "rrspContribution": 3500.00,
    "capitalGains": 1200.00,
    "confidenceScores": { "employmentIncome": 0.94, "rrspContribution": 0.87, "capitalGains": 0.76 }
  }'
```

**Response:**
```json
[
  {
    "field": "employmentIncome",
    "uploadedValue": 52000.00,
    "craValue": 48200.00,
    "delta": 3800.00,
    "severity": "HIGH",
    "requiresReview": true,
    "aiExplanation": "Likely an amended T4 slip not yet processed by CRA."
  }
]
```

---

### 3. Simulate RRSP Optimization

```bash
curl -X POST "http://localhost:8080/api/v1/optimization/rrsp?additionalContrib=5000" \
  -H "Content-Type: application/json" \
  -d '{
    "employmentIncome": 80000.00,
    "rrspContribution": 3000.00,
    "capitalGains": 0.00,
    "confidenceScores": { "employmentIncome": 1.0, "rrspContribution": 1.0, "capitalGains": 1.0 }
  }'
```

**Response:**
```json
{
  "additionalContribution": 5000.00,
  "currentTax": 14761.25,
  "optimizedTax": 13736.25,
  "estimatedSavings": 1025.00,
  "aiTradeoffExplanation": "Contributing an additional $5,000 to your RRSP reduces your taxable income by $5,000, saving approximately $1,025 in federal taxes at your 20.5% marginal rate. Consider your short-term liquidity needs before locking funds in a registered account."
}
```

---

### 4. Pre-Filing Risk Assessment

```bash
curl -X POST "http://localhost:8080/api/v1/risk/assess?hasDiscrepancy=true" \
  -H "Content-Type: application/json" \
  -d '{
    "employmentIncome": 52000.00,
    "rrspContribution": 12000.00,
    "capitalGains": 0.00,
    "confidenceScores": { "employmentIncome": 0.94, "rrspContribution": 0.87, "capitalGains": 1.0 }
  }'
```

**Response:**
```json
{
  "riskScore": 45,
  "riskLevel": "MEDIUM",
  "drivers": [
    "Employment income mismatch between uploaded slip and CRA data",
    "RRSP contribution is unusually large relative to employment income"
  ]
}
```

---

## Tax Bracket Model (2025 Federal)

| Income Range | Marginal Rate |
|---|---|
| $0 – $57,375 | 15% |
| $57,375 – $114,750 | 20.5% |
| $114,750 – $158,519 | 26% |
| $158,519 – $220,000 | 29% |
| $220,000+ | 33% |

Provincial tax is excluded from the MVP model.

---

## Running Tests

```bash
./mvnw test
```

Unit tests cover:
- `TaxBracketCalculatorTest` — bracket accuracy, monotonicity, marginal rate
- `ReconciliationServiceTest` — threshold logic, severity classification, multi-field detection
- `OptimizationServiceTest` — savings computation, floor at zero, AI explanation presence

---

## Design Principles

1. **Constructor injection only** — no `@Autowired` field injection
2. **Records for DTOs** — immutable, compact, serialization-friendly
3. **Builders for domain models** — explicit construction, no setters
4. **LLM for language, Java for math** — strict AI boundary enforcement
5. **JSON schema enforcement** — all LLM responses validated before use
6. **RFC 7807 error responses** — consistent ProblemDetail across all errors
7. **Configurable thresholds** — all tuning values in `application.yml`, no magic numbers
