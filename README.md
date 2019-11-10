# Receipts Analyzer Tool

## Description

Simulates the following enterprise scenario: periodically read customer's receipts from XML file and upload to DB (MySQL in this case).

## Prerequisites

* Java SE 8
* MySQL 5.7
* Maven 3.6
* Free port 8080

## Configuration

File: `src/main/java/resources/application.properties`.

### Flags

* `rat.import-dir` - Directory for look up the new files for upload.
* `rat.import-schedule-rate-ms` - Rate interval between scan & upload task.
* `spring.datasource.url` - Data source URL for MySQL.
* `spring.datasource.username` - MySQL Connection username.
* `spring.datasource.username` - MySQL Connection password.

## Run

1. `mvn spring-boot:run`
1. Navigate to `localhost:8080` in browser to view the Index page.

## Run with Docker

1. To build docker image: `docker build -t ost/rat .`
1. To run docker image: `docker run -d --name rat --net=host -v <data-dir>:/data/import -v <dir-with-application.properties>:/config ost/rat`
1. To delete docker container: `docker rm -ost/rat`

## Run with Docker Compose (Application & MySQL)

1. Create directory `mysql` for MySQL data.
2. Create directory `data/import` for files to import.
2. Create directory `config` with `application.properties` file and set the following params:
````
spring.datasource.url=jdbc:mysql://mysql:3306/rat?useUnicode=true&characterEncoding=utf8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
rat.import-dir=data/import
rat.import-schedule-rate-ms=600000
````
3. Run `docker-compose up`.