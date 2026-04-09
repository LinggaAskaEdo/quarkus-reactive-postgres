.PHONY: help build clean run test test-integration package package-native docker docker-jvm docker-legacy-jar docker-native docker-native-micro lint format flyway-up flyway-repair flyway-clean flyway-status

# Default target
.DEFAULT_GOAL := help

# Colors for help output
BLUE := \033[36m
RESET := \033[0m

##@ Build

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*##"; printf "\n${BLUE}Usage:${RESET} make ${BLUE}<target>${RESET}\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  ${BLUE}%-25s${RESET} %s\n", $$1, $$2 } /^##@/ { printf "\n${BLUE}%s${RESET}\n", substr($$0, 5) }' $(MAKEFILE_LIST)

build: ## Compile and build the application
	./mvnw clean compile

package: ## Package the application as a JAR
	./mvnw clean package -DskipTests

package-native: ## Package the application as a native executable
	./mvnw clean package -Pnative -DskipTests

package-uber: ## Package the application as an uber-JAR
	./mvnw clean package -Dquarkus.package.jar.type=uber-jar -DskipTests

##@ Run

run: ## Run the application in development mode
	./mvnw quarkus:dev

run-debug: ## Run the application in development mode with debug port 5005
	./mvnw quarkus:dev -Ddebug=5005

##@ Test

test: ## Run unit tests
	./mvnw clean test

test-integration: ## Run integration tests
	./mvnw verify -DskipITs=false

test-coverage: ## Run tests with code coverage report
	./mvnw clean verify jacoco:report

##@ Docker

docker: docker-jvm ## Build Docker images (JVM mode by default)

docker-jvm: ## Build Docker image (JVM mode)
	./mvnw clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.builder=docker
	docker build -f src/main/docker/Dockerfile.jvm -t quarkus-reactive-postgres:jvm .

docker-legacy-jar: ## Build Docker image (Legacy JAR mode)
	./mvnw clean package
	docker build -f src/main/docker/Dockerfile.legacy-jar -t quarkus-reactive-postgres:legacy-jar .

docker-native: ## Build Docker image (Native mode)
	./mvnw clean package -Pnative -DskipTests
	docker build -f src/main/docker/Dockerfile.native -t quarkus-reactive-postgres:native .

docker-native-micro: ## Build Docker image (Native Micro mode)
	./mvnw clean package -Pnative -DskipTests
	docker build -f src/main/docker/Dockerfile.native-micro -t quarkus-reactive-postgres:native-micro .

docker-run: ## Run the default Docker container (JVM mode)
	docker run -i --rm -p 8080:8080 quarkus-reactive-postgres:jvm

docker-run-native: ## Run the native Docker container
	docker run -i --rm -p 8080:8080 quarkus-reactive-postgres:native

##@ Cleanup

clean: ## Clean build artifacts
	./mvnw clean

##@ Code Quality

lint: ## Run code linting and check for issues
	./mvnw checkstyle:check

format: ## Format source code
	./mvnw formatter:format

validate: ## Validate the project structure and dependencies
	./mvnw validate

##@ Flyway

flyway-up: ## Apply pending database migrations
	@read -p "Database username [postgres]: " DB_USER; \
	read -s -p "Database password [postgres]: " DB_PASS; \
	echo; \
	mvn flyway:migrate \
	-Dflyway.url=jdbc:postgresql://localhost:5432/quarkus-reactive-postgres \
	-Dflyway.user=$${DB_USER:-postgres} \
	-Dflyway.password=$${DB_PASS:-postgres} \
	-Dflyway.locations=filesystem:src/main/resources/db/migration

flyway-repair: ## Repair the Flyway schema history table
	@read -p "Database username [postgres]: " DB_USER; \
	read -s -p "Database password [postgres]: " DB_PASS; \
	echo; \
	mvn flyway:repair \
	-Dflyway.url=jdbc:postgresql://localhost:5432/quarkus-reactive-postgres \
	-Dflyway.user=$${DB_USER:-postgres} \
	-Dflyway.password=$${DB_PASS:-postgres} \
	-Dflyway.locations=filesystem:src/main/resources/db/migration

flyway-clean: ## Clean the database schema (DROPS all tables) - Use with caution!
	@read -p "Database username [postgres]: " DB_USER; \
	read -s -p "Database password [postgres]: " DB_PASS; \
	echo; \
	echo "WARNING: This will drop all tables from the database!"; \
	read -p "Are you sure? (y/N): " CONFIRM; \
	if [ "$$CONFIRM" = "y" ] || [ "$$CONFIRM" = "Y" ]; then \
	mvn flyway:clean \
	-Dflyway.url=jdbc:postgresql://localhost:5432/quarkus-reactive-postgres \
	-Dflyway.user=$${DB_USER:-postgres} \
	-Dflyway.password=$${DB_PASS:-postgres} \
	-Dflyway.locations=filesystem:src/main/resources/db/migration \
	-Dflyway.cleanDisabled=false; \
	else \
	echo "Operation cancelled"; \
	fi

flyway-status: ## Show migration history and current state
	@read -p "Database username [postgres]: " DB_USER; \
	read -s -p "Database password [postgres]: " DB_PASS; \
	echo; \
	mvn flyway:info \
	-Dflyway.url=jdbc:postgresql://localhost:5432/quarkus-reactive-postgres \
	-Dflyway.user=$${DB_USER:-postgres} \
	-Dflyway.password=$${DB_PASS:-postgres} \
	-Dflyway.locations=filesystem:src/main/resources/db/migration
