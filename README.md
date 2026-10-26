# DC Bias Crime: Spark + Postgres + Metabase

A small end-to-end data project: load a public bias-crime dataset into Postgres, clean it with a Scala/Spark job, and explore it in Metabase. Everything runs locally with Docker.

## Architecture
## Stack

Scala 2.12, Apache Spark 3.5 (local mode), sbt, PostgreSQL 18, Metabase, Docker Compose.

## What the Spark job does

- Reads the raw table over JDBC in 4 parallel partitions, split on `objectid`.
- Converts text columns to real types: timestamps, integers, doubles.
- Turns blank strings into nulls and drops two columns that were 100% empty.
- Adds `targeted_group_clean`, which merges spelling variants of the same group (for example `Hispanic` and `Latino/Hispanic`), and `targeted_group_multiple`, which flags records with more than one group. The original column is kept.
- Writes the result to `bias_crime_clean`.

Code layout: `Postgres.scala` (connection and read/write helpers), `BiasCrimeCleaner.scala` (all cleaning rules), `CleanBiasCrimeJob.scala` (the job itself). Credentials come from environment variables, never from code.

## How I checked the work

- Row counts match between the raw and clean tables (1,852 rows each).
- Incidents per year match between the two tables.
- Failed conversions become nulls silently, so I counted nulls per column and checked each one against the blanks in the raw data.

## Findings

- Incidents per year were flat at about 65 to 80 from 2012 to 2015, rose sharply to 205 in 2018, then settled around 130 to 150 a year. 2026 is a partial year.
- The biggest bias types overall are Sexual Orientation (578), Ethnicity/National Origin (445) and Race (397).
- Caveat: these are reported incidents, so a rise can mean more incidents or more reporting. About 48% of records have no targeted group.

## Run it yourself

Requirements: Docker, Java 17, sbt. Commands below are for PowerShell.

1. Get the data. Download `Bias_Crime.csv` from DC Open Data (https://catalog.data.gov/dataset/bias-crime?from_hint=eyJxIjoiQ3JpbWUifQ%3D%3D) and put it in a `data/` folder. It is not committed to this repo.
2. Copy `.env.example` to `.env` and set a password.
3. Start Postgres and Metabase:
    docker compose up -d
4. Create the raw table and load the CSV:
    Get-Content sql\01_create_raw_table.sql | docker exec -i bias-pg psql -U postgres
    docker cp data\Bias_Crime.csv bias-pg:/tmp/Bias_Crime.csv
    docker exec bias-pg psql -U postgres -c �COPY bias_crime FROM �/tmp/Bias_Crime.csv� WITH (FORMAT csv, HEADER true);�
5. Set the connection variables, then run the Spark job:
    $env:PG_URL = �jdbc:postgresql://localhost:5432/postgres�
    $env:PG_USER = �postgres�
    $env:PG_PASSWORD = �your-password�
    sbt run
6. Open Metabase at http://localhost:4000 and add a PostgreSQL database with host `postgres`, port `5432`, database `postgres`.
