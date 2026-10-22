# pagopa-posgw-transactions-handler

PagoPA microservice that handles transactions' lifecycle and workflow for pos-gateway domain

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-posgw-transactions-handler&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-posgw-transactions-handler)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-posgw-transactions-handler&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-posgw-transactions-handler)


---

## Api Documentation 📖

See
the [OpenAPI 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-posgw-transactions-handler/main/api-spec/transactions-handler-api.yaml)

---

## Technology Stack

- Kotlin
- Spring Boot

---

## Start Project Locally 🚀

### Prerequisites

- Docker
- GitHub personal access token with `packages:read` permission

### GitHub Token Setup

To access the `pagopa-posgw-common` library from GitHub Packages, you need to set up authentication:

1. Create a GitHub personal access token with `packages:read` permission
2. Set the token as an environment variable:

```shell
export GITHUB_TOKEN=your_github_token_with_packages_read_permission
```

### Populate the environment

The microservice needs a valid `.env` file in order to be run.

If you want to start the application without too much hassle, you can just copy `.env.example` to get a good default
configuration with:

```shell
cp .env.example .env
```

#### environment variables

If you want to customize the application environment, reference this table:

| Variable name                                        | Description                                               | type     | default |
|------------------------------------------------------|-----------------------------------------------------------|----------|---------|
| `ACTIVE_PROFILE`                                     | Spring active profile                                     | string   | `local` |
| `ROOT_LOGGING_LEVEL`                                 | Root logging level                                        | string   | `INFO`  |
| `APP_LOGGING_LEVEL`                                  | Logging level for `it.pagopa` packages                    | string   | `INFO`  |
| `WEB_LOGGING_LEVEL`                                  | Logging level for Spring web                              | string   | `INFO`  |
| `SERVICE_BUS_ECOMMERCE_SYNC_QUEUE_NAME`              | Name of the eCommerce sync Service Bus queue              | string   |         |
| `SERVICE_BUS_ECOMMERCE_SYNC_CONNECTION_STRING`       | Connection string of the eCommerce sync queue             | string   |         |
| `SERVICE_BUS_ECOMMERCE_SYNC_RETRY_QUEUE_NAME`        | Name of the eCommerce sync retry Service Bus queue        | string   |         |
| `SERVICE_BUS_ECOMMERCE_SYNC_RETRY_CONNECTION_STRING` | Connection string of the eCommerce sync retry queue       | string   |         |
| `SERVICE_BUS_GEC_SYNC_QUEUE_NAME`                    | Name of the GEC sync Service Bus queue                    | string   |         |
| `SERVICE_BUS_GEC_SYNC_CONNECTION_STRING`             | Connection string of the GEC sync queue                   | string   |         |
| `SERVICE_BUS_GEC_SYNC_RETRY_QUEUE_NAME`              | Name of the GEC sync retry Service Bus queue              | string   |         |
| `SERVICE_BUS_GEC_SYNC_RETRY_CONNECTION_STRING`       | Connection string of the GEC sync retry queue             | string   |         |
| `SERVICE_BUS_SESSION_POLL_QUEUE_NAME`                | Name of the session poll Service Bus queue                | string   |         |
| `SERVICE_BUS_SESSION_POLL_CONNECTION_STRING`         | Connection string of the session poll queue               | string   |         |
| `SERVICE_BUS_SESSION_EXPIRE_QUEUE_NAME`              | Name of the session expire Service Bus queue              | string   |         |
| `SERVICE_BUS_SESSION_EXPIRE_CONNECTION_STRING`       | Connection string of the session expire queue             | string   |         |
| `SERVICE_BUS_SYSTEM_TELEMETRY_TOPIC_NAME`            | Name of the system telemetry Service Bus topic            | string   |         |
| `SERVICE_BUS_SYSTEM_TELEMETRY_CONNECTION_STRING`     | Connection string of the system telemetry topic           | string   |         |
| `REDIS_HOST`                                         | Hostname of the Redis instance                            | string   |         |
| `REDIS_PORT`                                         | Port the Redis instance listens to                        | number   |         |
| `REDIS_PASSWORD`                                     | Password used to authenticate to Redis                    | string   |         |
| `REDIS_SSL_ENABLED`                                  | Whether to use SSL for the Redis connection               | boolean  |         |
| `REDIS_TIMEOUT`                                      | Redis command timeout                                     | duration |         |
| `REDIS_CONNECTION_TIMEOUT`                           | Redis connection timeout                                  | duration |         |
| `PAYMENT_SESSION_REDIS_TTL_SECONDS`                  | TTL in seconds of payment session entries stored in Redis | number   |         |

### Build Docker Image

```sh
export GITHUB_TOKEN=your_github_token_with_packages_read_permission
docker build --secret id=GITHUB_TOKEN,env=GITHUB_TOKEN -t pagopa-posgw-transactions-handler .
```

### Run with Docker Compose

Check that the .env file exists and is populated with the correct values (
see [Populate the environment](#populate-the-environment)).

Then start [posgw-local](https://github.com/pagopa/pagopa-posgw-local) project with:

```sh
docker compose up --scale pagopa-posgw-transactions-handler=0
```

This way you can use the locally build service version with posgw local project, otherwise see posgw local project
README to start all services using docker builds

---

## Develop Locally 💻

### Prerequisites

- Git
- Gradle
- JDK 25

### Run the project

```shell
$ (set -a; source .env.example; set +a && ./gradlew bootRun)
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

### Pos-gateway Common Library

The service uses the `posgw-common` library which is distributed via GitHub Packages. The library version is configured
in `build.gradle.kts`.

This property maps the `posgw-common` version:

````
val posgwCommonVersion = "x.y.z" -> valued with posgw common wanted version
````

The library is automatically downloaded from GitHub Packages during the build process using the configured GitHub token.

---

### Dependency management 🔧

For support reproducible build this project has the following gradle feature enabled:

- [dependency lock](https://docs.gradle.org/9.5.1/userguide/dependency_locking.html)
- [dependency verification](https://docs.gradle.org/9.5.1/userguide/dependency_verification.html)

#### Dependency lock

This feature use the content of `gradle.lockfile` to check the declared dependencies against the locked one.

If a transitive dependencies have been upgraded the build will fail because of the locked version mismatch.

The following command can be used to upgrade dependency lockfile:

```shell
./gradlew dependencies --write-locks 
```

Running the above command will cause the `gradle.lockfile` to be updated against the current project dependency
configuration

#### Dependency verification

This feature is enabled by adding the gradle `./gradle/verification-metadata.xml` configuration file.

Perform checksum comparison against dependency artifact (jar files, zip, ...) and metadata (pom.xml, gradle module
metadata, ...) used during build
and the ones stored into `verification-metadata.xml` file raising error during build in case of mismatch.

The following command can be used to recalculate dependency checksum:

```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build 
```

In the above command the `clean`, `spotlessApply` `build` tasks where chosen to be run
in order to discover all transitive dependencies used during build and also the ones used during
spotless apply task used to format source code.

The above command will upgrade the `verification-metadata.xml` adding all the newly discovered dependencies' checksum.
Those checksum should be checked against a trusted source to check for corrispondence with the library author published
checksum.

`/gradlew --write-verification-metadata sha256` command appends all new dependencies to the verification files but does
not remove
entries for unused dependencies.

This can make this file grow every time a dependency is upgraded.

To detect and remove old dependencies make the following steps:

1. Delete, if present, the `gradle/verification-metadata.dryrun.xml`
2. Run the gradle write-verification-metadata in dry-mode (this will generate a verification-metadata-dryrun.xml file
   leaving untouched the original verification file)
3. Compare the verification-metadata file and the verification-metadata.dryrun one checking for differences and removing
   old unused dependencies

The 1-2 steps can be performed with the following commands

```Shell
rm -f ./gradle/verification-metadata.dryrun.xml 
./gradlew --write-verification-metadata sha256 clean spotlessApply build --dry-run
```

The resulting `verification-metadata.xml` modifications must be reviewed carefully checking the generated
dependencies checksum against official websites or other secure sources.

If a dependency is not discovered during the above command execution it will lead to build errors.

You can add those dependencies manually by modifying the `verification-metadata.xml`
file adding the following component:

```xml

<verification-metadata>
    <!-- other configurations... -->
    <components>
        <!-- other components -->
        <component group="GROUP_ID" name="ARTIFACT_ID" version="VERSION">
            <artifact name="artifact-full-name.jar">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
            <artifact name="artifact-pom-file.pom">
                <sha256 value="sha value"
                        origin="Description of the source of the checksum value"/>
            </artifact>
        </component>
    </components>
</verification-metadata>
```

Add those components at the end of the components list and then run the

```shell
./gradlew --write-verification-metadata sha256 clean spotlessApply build 
```

that will reorder the file with the added dependencies checksum in the expected order.

Finally, you can add new dependencies both to gradle.lockfile writing verification metadata running

```shell
 ./gradlew dependencies --write-locks --write-verification-metadata sha256
```

For more information read the
following [article](https://docs.gradle.org/9.5.1/userguide/dependency_verification.html#sec:checksum-verification)

## Contributors 👥

Made with ❤️ by PagoPA S.p.A.

### Maintainers

See `CODEOWNERS` file
