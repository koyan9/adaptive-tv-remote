# Quick Start

This guide covers local setup, running the app, and a simple deployment flow.

## Prerequisites

- JDK 17
- Network access to real TVs/gateways if you plan to use the `real` profile

## Local Setup (Mock)

Windows:

```powershell
.\mvnw.cmd -q spring-boot:run
```

macOS/Linux:

```bash
./mvnw -q spring-boot:run
```

Open `http://localhost:8080`.

Notes:

- `mock` is the default profile.
- Use `run-local.cmd` to force the mock profile on Windows.

## Real Integrations (Production-Like)

1. Configure `src/main/resources/application-real.yml` with real endpoints and credentials.
2. Run the service in the `real` profile.

Windows:

```powershell
.\run-real.cmd
```

macOS/Linux:

```bash
SPRING_PROFILES_ACTIVE=real java -jar target/adaptive-tv-remote-0.1.0.jar
```

You can also supply external config files:

- `SPRING_CONFIG_ADDITIONAL_LOCATION=/path/to/`
- `SPRING_CONFIG_LOCATION=/path/to/application-real.yml`

## Verify

- Open `http://localhost:8080`
- Check health at `http://localhost:8080/api/remote/health/integrations`
- Check config summary at `http://localhost:8080/api/remote/integrations`

## Build for Deployment

Windows:

```powershell
.\mvnw.cmd -q -DskipTests package
```

macOS/Linux:

```bash
./mvnw -q -DskipTests package
```

The jar will be at `target/adaptive-tv-remote-0.1.0.jar`.

## Deploy (Jar)

1. Copy the jar and configuration to the host.
2. Set `SPRING_PROFILES_ACTIVE=real` (or keep mock for demo).
3. Run:

Windows:

```powershell
set SPRING_PROFILES_ACTIVE=real
java -jar target\adaptive-tv-remote-0.1.0.jar
```

macOS/Linux:

```bash
SPRING_PROFILES_ACTIVE=real java -jar target/adaptive-tv-remote-0.1.0.jar
```

Optional runtime settings:

- `server.port=8080` (or another port)
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`

## Configuration Hints

Minimum real-mode config typically includes:

- `remote.integration.default-mode: real`
- `remote.integration.strict-mode: true`
- `remote.integration.adapter-modes.sony-lan: real`
- `remote.integration.adapter-modes.generic-ir: real`
- `remote.integration.adapter-modes.generic-hdmi-cec: real`
- Sony endpoints + PSK
- Gateway endpoints + auth token + hub ID
- Optional IR code map and CEC command map

## Troubleshooting

- `integration.disabled`: check `remote.integration.*.enabled` flags
- `integration.config.missing`: verify endpoints/credentials
- `integration.transport.timeout`: check device reachability and network

## Data Storage

- Default storage is local H2.
- For production, configure an external database via `spring.datasource.*`.
