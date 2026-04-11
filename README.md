# quarkus-reactive-postgres

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Prerequisites

- Java 21+
- Docker (for PostgreSQL and Keycloak)

## Database Setup

Start a PostgreSQL container:

```shell
docker run -d --name postgres-docker -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=quarkus-reactive-postgres -p 5432:5432 postgres:18
```

Run database migrations:

```shell
make flyway-up
```

## Keycloak Setup

This project uses Keycloak for OAuth2/OIDC authentication. All API endpoints (except `/auth/*`) require a valid Bearer token.

### 1. Start Keycloak Container

```shell
docker run -d \
 --name keycloak-docker \
 -p 127.0.0.1:8080:8080 \
 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
 -e KC_BOOTSTRAP_ADMIN_PASSWORD=change_me \
 --network otis-network \
 quay.io/keycloak/keycloak:latest \
 start \
 --hostname=localhost \
 --http-enabled=true \
 --db=postgres \
 --features=token-exchange \
 --db-url=jdbc:postgresql://postgres-docker:5432/keycloak \
 --db-username=postgres \
 --db-password=postgres
```

> **Note:** The Keycloak database must exist in PostgreSQL. Create it with:
>
> ```shell
> docker exec postgres-docker psql -U postgres -c "CREATE DATABASE keycloak;"
> ```

### 2. Configure Realm and Client

1. Open Keycloak Admin Console: `http://localhost:8080` (admin/change_me)
2. Create a new realm named `quarkus`
3. Create a confidential client named `quarkus-app`:
   - **Client authentication**: On
   - **Valid redirect URIs**: `*`
   - Note the generated **Client secret**
4. Set the client secret in `application.yml` or via `KEYCLOAK_CLIENT_SECRET` env var

### 3. Register and Login

Users can register and login directly via the API (no manual Keycloak UI needed):

```shell
# Register
curl -X POST http://localhost:8181/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"user1@test.com","password":"password123"}'

# Login
curl -X POST http://localhost:8181/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}'
```

### Configuration

| Env Variable              | Default                 | Description                                     |
| ------------------------- | ----------------------- | ----------------------------------------------- |
| `KEYCLOAK_URL`            | `http://localhost:8080` | Keycloak server URL                             |
| `KEYCLOAK_REALM`          | `quarkus`               | Keycloak realm name                             |
| `KEYCLOAK_CLIENT_ID`      | `quarkus-app`           | OAuth2 client ID                                |
| `KEYCLOAK_CLIENT_SECRET`  | `secret`                | OAuth2 client secret                            |
| `KEYCLOAK_ADMIN_USERNAME` | `admin`                 | Keycloak admin username (for user registration) |
| `KEYCLOAK_ADMIN_PASSWORD` | `change_me`             | Keycloak admin password (for user registration) |

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8181/q/dev/>.

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

### Authentication

| Method | Path             | Description             |
| ------ | ---------------- | ----------------------- |
| POST   | `/auth/register` | Register a new user     |
| POST   | `/auth/login`    | Login and get JWT token |

#### Register User

```shell
curl -X POST http://localhost:8181/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securepassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Login

```shell
curl -X POST http://localhost:8181/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "securepassword123"
  }'
```

Response:

```json
{
  "access_token": "eyJ...",
  "expires_in": 300,
  "refresh_token": "eyJ...",
  "token_type": "Bearer"
}
```

#### Use Token in API Requests

```shell
TOKEN=$(curl -s -X POST http://localhost:8181/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"securepassword123"}' \
  | jq -r .access_token)

curl -H "Authorization: Bearer $TOKEN" http://localhost:8181/fruits
```

Import the Postman collection from `etc/quarkus-reactive-postgres.postman_collection.json` for ready-made requests.

## Fruit Scheduler

A background job that periodically fetches random fruit names from the [FruityVice API](https://www.fruityvice.com/) and inserts them into the database.

### Scheduler Configuration

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

## WebClient Configuration

HTTP calls use a shared Vert.x `WebClient` (produced by `WebClients.java`), configurable via `application.yml`:

```yaml
webclient:
  max-pool-size: 20 # Maximum number of HTTP connections in the pool
  connect-timeout: 5000 # Connection timeout in milliseconds
  idle-timeout: 60000 # Close idle connections after this time (ms)
  max-wait-queue-size: 5000 # Maximum requests waiting for a connection
```

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

## Externalized SQL (ELSql)

SQL queries are externalized to `.elsql` files under `src/main/resources/sql/` using the [ELSql](https://github.com/OpenGamma/ElSql) library. This keeps SQL out of Java code for easier maintenance and review.

### Structure

```text
src/main/resources/sql/
├── fruits.elsql       # All Fruit repository queries
└── employees.elsql    # All Employee repository queries
```

### ELSql Syntax

```text
@NAME(FindById)
  SELECT id, name FROM fruits WHERE id = :id

@NAME(Create)
  INSERT INTO fruits (id, name) VALUES (:id, :name) RETURNING id
```

### Usage in Java

```java
// Simple named parameter query
String sql = sqlManager.getSql("FindById", Map.of("id", someId));
client.preparedQuery(sql).execute(Tuple.of(someId));

// Query without parameters
String sql = sqlManager.getSql("FindAll");
client.query(sql).execute();
```

### Dynamic SQL

For queries requiring dynamic structure (ORDER BY columns, LIMIT/OFFSET), SQL templates use `%s`/`%d` placeholders filled safely in Java with validated values:

```java
String sql = sqlManager.getSql("FindAllPaged")
    .formatted(safeColumn, safeDirection, limit, offset);
```

Only whitelisted column names and sort directions are allowed to prevent SQL injection.

## Project Structure

This project follows a domain-driven design layout with clean architecture principles:

```text
src/main/java/org/otis/
├── shared/                      # Cross-cutting concerns
│   ├── constant/                # Constants and enums
│   ├── dto/                     # Shared DTOs (requests, responses, paging)
│   └── util/                    # Helper utilities (LoggingFilter, RequestContext, SqlManager, WebClients)
│
├── auth/                        # Auth bounded context
│   └── usecase/                 # RegisterUser, LoginUser
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

src/main/resources/
├── sql/                         # Externalized SQL queries (ELSql)
│   ├── fruits.elsql             # Fruit repository queries
│   └── employees.elsql          # Employee repository queries
└── db/migration/                # Flyway migration scripts
```

### Architecture Principles

- **Domain-driven design** — each business concept lives in its own bounded context
- **Repository pattern** — domain interfaces abstracted from infrastructure
- **Use case classes** — single-responsibility, testable business logic
- **Constructor injection** — explicit dependencies, no field injection
- **Externalized SQL** — SQL queries in `.elsql` files via ELSql library
- **Vert.x WebClient** — HTTP calls via shared `WebClient` producer (configurable timeouts & pool)
- **JSON logging** — structured logs with `req_id` (UUID v7) for request correlation
- **Context propagation** — `req_id` propagated across async thread boundaries

## Related Guides

- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern
