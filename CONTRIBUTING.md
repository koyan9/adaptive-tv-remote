# Contributing

## Local setup

- Use Java `17+`
- Prefer the committed Maven Wrapper
- Recommended commands:
  - Windows: `mvnw.cmd -q test`
  - macOS / Linux: `./mvnw -q test`
- Mock integration suite (tagged):
  - Windows: `mvnw.cmd -q test -Djunit.jupiter.tags=mock-integration`
  - macOS / Linux: `./mvnw -q test -Djunit.jupiter.tags=mock-integration`

## Workflow

- Keep changes focused and easy to review
- Add or update tests for behavior changes
- Update `README.md` or `docs/` when contributor-facing behavior changes
- Preserve the separation between transport adapters, brand adapters, and protocol clients

## Project layout

- `src/main/java/.../api`: REST endpoints
- `src/main/java/.../service`: routing and orchestration
- `src/main/java/.../adapter`: transport adapters
- `src/main/java/.../brand`: brand-specific command mapping
- `src/main/java/.../integration`: mock/real protocol client skeletons
- `src/main/resources/static`: mobile web remote UI

## Before opening a pull request

- Run `mvnw.cmd -q test` or `./mvnw -q test`
- If you touched mock integration tests, run the tagged suite as well
- If you change configuration or integration behavior, update `docs/INTEGRATIONS.md`
- If you change `application-real.yml` IR code mapping, update the docs template with `powershell -ExecutionPolicy Bypass -File scripts/update-ir-template.ps1`
- Summarize scope, validation, and any follow-up work in the PR description



