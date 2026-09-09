# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Feature - Consistent merged identity selection across generators

- Unified merged identity selection across OpenAPI 2.0, OpenAPI 3.0.1, and Postman
  when multiple snippets map to the same path and method.
- Primary candidates are selected deterministically with success-first priority:
  `200`, `201`, `202`, `204`, other `2xx`, then non-blank `summary`, non-blank
  `description`, and lexical `operationId` tie-break.
- OpenAPI 2.0 and OpenAPI 3.0.1 use the same merged `operationId` derivation
  (common prefix across primary candidates, falling back to the top-priority
  candidate's own `operationId` when no common prefix exists), and Postman
  top-level item `id` uses the same merged ID rule.
- Replaced the previous sorted-concatenation fallback (e.g. `firstsecond`) with
  the top-priority candidate's own `operationId`, since concatenating unrelated
  identifiers produced unreadable, codegen-hostile names for the common case of
  multiple snippets legitimately documenting the same path and method.
- Added test coverage for success-vs-error selection, `200` over `201` priority,
  and no-common-prefix fallback behavior, plus README documentation.

### Breaking

- This feature can change generated identifiers for merged endpoints (`operationId`
  in OpenAPI 2.0/3.0.1 and item `id` in Postman), which may require updates in
  generated clients and snapshot-based artifact comparisons. This includes
  endpoints that previously fell back to sorted concatenation, which now use the
  top-priority candidate's own `operationId` instead.

## [0.20.1](https://github.com/ePages-de/restdocs-api-spec/tree/0.20.1) - 2026-04-20

### Changed

- Declared `spring-boot-jackson2` version aligned with Spring Boot version (#296).

## [0.20.0](https://github.com/ePages-de/restdocs-api-spec/tree/0.20.0) - 2026-03-10

### Changed

- Fixed Gradle plugin publish by using explicit dependency versions for plugin POM
  generation.

## [0.19.5](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.5) - 2025-06-16

### Changed

- Updated Sonatype staging API endpoint configuration for publishing (#285).

## [0.19.4](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.4) - 2024-09-30

### Changed

- Made classes in `restdocs-api-spec` visible for `restassured` module usage (#273).

## [0.19.3](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.3) - 2024-05-06

### Changed

- Enabled GitHub Actions workflows on `maintenance/**` branches (#267).

## [0.19.2](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.2) - 2024-02-21

### Fixed

- Fixed null `Schema` handling (#260).

## [0.19.1](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.1) - 2024-02-14

### Changed

- Kotlin formatting cleanup (#258).

## [0.19.0](https://github.com/ePages-de/restdocs-api-spec/tree/0.19.0) - 2023-09-25

### Fixed

- Fixed regex extraction from pattern constraints (#247).

## [0.18.4](https://github.com/ePages-de/restdocs-api-spec/tree/0.18.4) - 2023-09-04

### Changed

- Applied `optional` as `nullable` in generated schemas (#245).

## [0.18.3](https://github.com/ePages-de/restdocs-api-spec/tree/0.18.3) - 2023-08-25

### Changed

- Applied field-level optionality handling in schema generation (#244).

## [0.17.1](https://github.com/ePages-de/restdocs-api-spec/tree/0.17.1) - 2023-01-31

### Changed

- Continued Spring REST Docs 3.x support line (see related support work in #225).

## [0.16.4](https://github.com/ePages-de/restdocs-api-spec/tree/0.16.4) - 2023-01-05

### Changed

- Made classes in `restdocs-api-spec` modules visible (#223).

## [0.10.0](https://github.com/ePages-de/restdocs-api-spec/tree/0.10.0) - 2020-09-16

### Changed

- OpenAPI 3: derive merged `operationId` from common snippet name prefix
  (fallback to concatenation) (#141).

## [0.8.0](https://github.com/ePages-de/restdocs-api-spec/tree/0.8.0) - 2019-01-12

### Added

- Added Postman collection generation support (#71).
