# RadNav Connector

This package is the standalone connector component in the RadNav monorepo.

## Release model

- The connector is built and shipped independently from the rest of the repository.
- Its version is tracked in `connector/src/connector/_version.py`.
- Docker images and release artifacts should be tagged with the connector version, for example:
  - `radnav-connector:0.1.0`
  - `ghcr.io/aiworkhive/radnav-connector:0.1.0`

## Boundary rule

The connector must not import from `core.*` or any core internal module. CI enforces this rule.
