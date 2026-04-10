# quarkus-reactive-postgres

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Prerequisites

- Java 21+
- Docker (for PostgreSQL)

## Database Setup

Start a PostgreSQL container:

```shell
docker run -d --name postgres-docker -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=quarkus-reactive-postgres -p 5432:5432 postgres:18
```

Run database migrations:

```shell
make flyway-up
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-reactive-postgres-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Database Migrations

Manage database schema with Flyway:

```shell
make flyway-up       # Apply pending migrations
make flyway-status   # Show migration history
make flyway-repair   # Repair schema history (e.g., after checksum changes)
make flyway-clean    # Drop all tables (use with caution!)
```

## API Endpoints

### Fruits

| Method | Path           | Description     |
| ------ | -------------- | --------------- |
| GET    | `/fruits`      | Get all fruits  |
| GET    | `/fruits/{id}` | Get fruit by ID |
| POST   | `/fruits`      | Create a fruit  |
| PATCH  | `/fruits`      | Update a fruit  |
| DELETE | `/fruits/{id}` | Delete a fruit  |

### Employees

| Method | Path           | Description             |
| ------ | -------------- | ----------------------- |
| POST   | `/employees/1` | Get paginated employees |
| POST   | `/employees/2` | Get paginated employees |

Import the Postman collection from `etc/quarkus-reactive-postgres.postman_collection.json` for ready-made requests.

## Fruit Scheduler

A background job that periodically fetches random fruit names from the [FruityVice API](https://www.fruityvice.com/) and inserts them into the database.

### Configuration

```yaml
fruit:
  scheduler:
    interval: 1m # Scheduler interval (e.g., 1m, 5m, 1h)
    min-insert: 10 # Minimum fruits to insert per run (minimum: 3)
    enabled: true # Enable/disable the scheduler
    api-url: https://www.fruityvice.com/api/fruit/all # API endpoint
```

### Behavior

- Fetches all fruits from the 3rd party API and shuffles them randomly
- Inserts a configurable minimum number of fruits per run
- Uses bulk insert with `ON CONFLICT DO NOTHING` to skip duplicates
- Each run logs a unique `req_id` (UUID v7, 8 chars) and `processTimeMs`

## Logging

All logs are output in JSON format with the following structure:

```json
{
  "timestamp": "2026-04-10T09:44:45.068+07:00",
  "level": "INFO",
  "message": "event=END httpMethod=GET requestPath=/fruits statusCode=200 processTimeMs=45",
  "threadName": "vert.x-eventloop-thread-2",
  "mdc": { "req_id": "019d7547" },
  "loggerName": "org.otis.shared.util.LoggingFilter"
}
```

### Fields

| Field           | Description                                                             |
| --------------- | ----------------------------------------------------------------------- |
| `req_id`        | Request ID (UUID v7, 8 chars) — unique per API request or scheduler run |
| `processTimeMs` | Duration of the request/scheduler run in milliseconds                   |

The `req_id` is propagated across async thread boundaries so all log lines for a single operation share the same ID.

## Project Structure

This project follows a domain-driven design layout with clean architecture principles:

```text
src/main/java/org/otis/
├── shared/                      # Cross-cutting concerns
│   ├── constant/                # Constants and enums
│   ├── dto/                     # Shared DTOs (requests, responses, paging)
│   └── util/                    # Helper utilities (LoggingFilter, RequestContext)
│
├── fruit/                       # Fruit bounded context
│   ├── domain/                  # Entity and repository interface
│   ├── infrastructure/          # Repository implementation + scheduler service
│   └── usecase/                 # Single-responsibility use case classes
│
├── employee/                    # Employee bounded context
│   ├── domain/
│   ├── infrastructure/
│   └── usecase/
│
└── resource/                    # REST controllers (entry points)
```

### Architecture Principles

- **Domain-driven design** — each business concept lives in its own bounded context
- **Repository pattern** — domain interfaces abstracted from infrastructure
- **Use case classes** — single-responsibility, testable business logic
- **Constructor injection** — explicit dependencies, no field injection
- **Lombok** — reduces boilerplate with `@Data`, `@Getter`, `@Setter`
- **JSON logging** — structured logs with `req_id` (UUID v7) for request correlation
- **Context propagation** — `req_id` propagated across async thread boundaries

## Related Guides

- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern
