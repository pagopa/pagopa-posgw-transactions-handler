# pagopa-posgw-transactions-handler
PagoPA microservice that handles transactions' lifecycle and workflow for pos-gateway domain

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-posgw-transactions-handler&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-posgw-transactions-handler)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-posgw-transactions-handler&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-posgw-transactions-handler)


---

## Api Documentation 📖

TODO

---

## Technology Stack

- Kotlin
- Spring Boot

---

## Start Project Locally 🚀

### Prerequisites

- Docker

### Populate the environment

The microservice needs a valid `.env` file in order to be run.

If you want to start the application without too much hassle, you can just copy `.env.example` to get a good default configuration with:

```shell
cp .env.example .env
```

### Build Docker Image
```sh
docker build -t pagopa-posgw-transactions-handler .
```

### Run with Docker Compose
Check that the .env file exists and is populated with the correct values (see [Populate the environment](#populate-the-environment)).


Then start [posgw-local](https://github.com/pagopa/pagopa-posgw-local) project with:

```sh
docker compose up --scale pagopa-posgw-transactions-handler=0
```
This way you can use the locally build service version with posgw local project, otherwise see posgw local project README to start all services using docker builds

---

## Develop Locally 💻

### Prerequisites

- Git
- Gradle
- JDK 25

### Run the project

```shell
$ (set -a; source .env.local; set +a && ./gradlew bootRun)
```

### Code formatting

Code formatting checks are automatically performed during build phase.
If the code is not well formatted an error is raised blocking the gradle build.

Helpful commands:

```sh
$ ./gradlew spotlessCheck # --> used to perform format checks
$ ./gradlew spotlessApply # --> used to format all misformatted files
```

### Testing 🧪

#### Unit testing

To run the **Junit** tests:

```shell
$ ./gradlew test
```

#### Integration testing

TODO

---

## Contributors 👥

Made with ❤️ by PagoPA S.p.A.

### Maintainers

See `CODEOWNERS` file
