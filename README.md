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

