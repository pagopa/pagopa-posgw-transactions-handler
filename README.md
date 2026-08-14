# pagopa-posgw-transactions-handler
PagoPA microservice that handles transactions' lifecycle and workflow for pos-gateway domain

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

### Run docker container

```shell
$ docker compose up --build
```

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
