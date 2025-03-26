# Currency Exchange API

This REST API manages currencies and exchange rates, enabling viewing, editing, and currency conversions.

No web interface is included.

## What You Need to Know

- Java: Collections, OOP
- Pattern: MVC(S)
- Build Tools: Maven/Gradle
- Backend: Java Servlets
- HTTP: GET/POST, response codes
- REST API, JSON
- Database: SQLite, JDBC
- Deployment: Cloud, Linux, Tomcat
- No frameworks used.

## Project Motivation

- MVC introduction
- REST API: Resource naming, HTTP codes
- SQL: Basic syntax, table creation

## Database

SQLite is used for easy deployment.

The database includes `Currencies` (ID, code, full name, symbol; with primary key and unique code index) and `ExchangeRates` (IDs of base and target currencies, rate; with primary key and unique currency pair index).  Rate is stored as Decimal(6).

World currency codes: [ISO Currency Codes](https://www.iban.com/currency-codes).

## REST API

REST API methods implement CRUD (Create, Read, Update) operations.  Delete is omitted.


## Deployment

Deploy the WAR to Tomcat on a remote server. No external SQL database needed.

Steps:

1. Build WAR artifact
2. Rent Linux cloud server
3. Install JRE and Tomcat
4. Install WAR in Tomcat admin
5. Access app at http://$server_ip:8080/$app_root_path

## Work Plan

1. Java backend stub (javax.servlet/jakarta.servlet)
2. Create/populate DB tables
3. Implement REST methods for currencies/rates
4. Implement currency exchange
5. Deploy to remote server

## Test Frontend

A test frontend is available at: [https://github.com/zhukovsd/currency-exchange-frontend](https://github.com/zhukovsd/currency-exchange-frontend).

The application is deployed on Digital Ocean.
