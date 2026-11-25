# ReconX – Bank Statement Reconciliation System

ReconX is a Java-based financial transaction reconciliation application built using Spring Boot and Spring Batch. It compares internal ledger transactions with bank statement records, identifies discrepancies, detects duplicate transactions, and stores reconciliation results in an H2 database.

## Features

- **CSV Processing:** Reads internal ledger and bank statement data from CSV files.
- **Automatic Reconciliation:** Matches transactions using transaction IDs and amounts.
- **Mismatch Detection:** Identifies transactions with different amounts.
- **Missing Transaction Detection:** Identifies transactions present in one source but missing from the other.
- **Duplicate Detection:** Detects duplicate transaction IDs in bank statements.
- **Batch Processing:** Uses Spring Batch to process reconciliation jobs.
- **Database Persistence:** Stores reconciliation results using Spring Data JPA and H2.
- **REST APIs:** Provides endpoints to execute reconciliation, retrieve summaries, and view exceptions.

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Batch
- Spring Data JPA
- Hibernate
- H2 Database
- Maven
- REST APIs
- CSV

## Reconciliation Results

The application currently processes sample ledger and bank statement data and produces:

| Status | Count |
|---|---:|
| MATCHED | 8 |
| MISMATCH | 1 |
| MISSING_IN_BANK | 1 |
| MISSING_IN_LEDGER | 2 |
| DUPLICATE_IN_BANK | 1 |
| **Total** | **13** |

## REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/recon/run` | Executes the reconciliation job |
| GET | `/api/recon/summary` | Retrieves reconciliation summary |
| GET | `/api/recon/exceptions` | Retrieves exception records |

### Example API Requests

Execute reconciliation:

```http
POST http://localhost:8080/api/recon/run
```

Retrieve summary:

```http
GET http://localhost:8080/api/recon/summary
```

Retrieve exceptions:

```http
GET http://localhost:8080/api/recon/exceptions
```

## Database

The application uses an H2 file-based database to persist reconciliation results.

H2 Console:

```text
http://localhost:8080/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/recondb
```

Username: `sa`

Password: Leave blank.

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/sudayagiri5/ledger-recon.git
```

2. Navigate to the project directory:

```bash
cd ledger-recon
```

3. Build the project:

```bash
mvn clean install
```

4. Run the Spring Boot application from your IDE or using:

```bash
mvn spring-boot:run
```

5. Execute the reconciliation API:

```bash
curl -X POST http://localhost:8080/api/recon/run
```

## Project Objective

The objective of ReconX is to demonstrate how financial transaction reconciliation can be automated using Java, Spring Boot, batch processing, database persistence, and REST APIs.


## Project Screenshots

### Reconciliation Summary
![Reconciliation Summary](screenshots/recon-summary.png)

### Exception Records
![Exception Records - Part 1](screenshots/recon-exceptions-1.png)

![Exception Records - Part 2](screenshots/recon-exceptions-2.png)

### H2 Database Records
![H2 Database Records](screenshots/recon-database.png)