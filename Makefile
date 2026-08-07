.PHONY: dev run install test build db-up db-down docker-up docker-down

dev: db-up install run

run:
	set -a; . ./.env; set +a; ./mvnw -pl ecommerce-api spring-boot:run

install:
	./mvnw install -DskipTests

test:
	./mvnw test

build:
	./mvnw clean verify

db-up:
	docker compose up -d postgres

db-down:
	docker compose down

docker-up:
	docker compose up -d --build

docker-down:
	docker compose down
