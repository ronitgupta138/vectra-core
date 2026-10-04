.PHONY: all build test run clean bench docker-build docker-up docker-down

MVNW := ./mvnw

all: build

build:
	$(MVNW) clean package -DskipTests

test:
	$(MVNW) test

bench:
	$(MVNW) test -Dtest=VectorEngineBenchmarkTest

run:
	$(MVNW) spring-boot:run

clean:
	$(MVNW) clean

docker-build:
	docker build -t vectra-core:latest .

docker-up:
	docker compose up -d

docker-down:
	docker compose down
