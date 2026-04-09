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

## Related Guides

- RESTEasy Reactive ([guide](https://quarkus.io/guides/resteasy-reactive)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- Reactive PostgreSQL client ([guide](https://quarkus.io/guides/reactive-sql-clients)): Connect to the PostgreSQL database using the reactive pattern
