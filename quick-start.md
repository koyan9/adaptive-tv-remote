# Quick Start (Production)

This guide shows the minimum steps to build and run Adaptive TV Remote in a production-like environment using the `real` profile. It assumes you have real devices or a gateway and want to point the service at those endpoints.

## Prerequisites

- JDK 17
- Network access to Sony TV and/or gateway endpoints

## Build

Windows:

```powershell
.\mvnw.cmd -q -DskipTests package
```

macOS/Linux:

```bash
./mvnw -q -DskipTests package
```

The jar will be at `target/adaptive-tv-remote-0.1.0.jar`.

## Configure Real Integrations

Edit `src/main/resources/application-real.yml` (or provide an external config file at deploy time) and fill in real endpoints and credentials.

External config options:

- `SPRING_CONFIG_ADDITIONAL_LOCATION=/path/to/`
- `SPRING_CONFIG_LOCATION=/path/to/application-real.yml`

Minimum config to set:

- `remote.integration.default-mode: real`
- `remote.integration.adapter-modes.sony-lan: real`
- `remote.integration.adapter-modes.generic-ir: real`
- `remote.integration.adapter-modes.generic-hdmi-cec: real`
- Sony endpoints + PSK
- Gateway endpoints + auth + hub ID
- Optional IR code map and CEC command map

Example:

```yml
remote:
  integration:
    default-mode: real
    adapter-modes:
      sony-lan: real
      generic-ir: real
      generic-hdmi-cec: real
    sony:
      enabled: true
      endpoint: "http://<sony-ip>/sony/accessControl"
      pre-shared-key: "<sony-psk>"
      ircc-endpoint: "http://<sony-ip>/sony/ircc"
    gateway:
      enabled: true
      endpoint: "http://<gateway-host>"
      infrared-endpoint: "http://<gateway-host>/api/infrared/send"
      hdmi-cec-endpoint: "http://<gateway-host>/api/cec/send"
      hub-id: "<hub-id>"
      auth-token: "<gateway-token>"
      ir-codes:
        tcl-q7-home:
          protocol: NEC
          bits: 32
          data: "0x00FF48B7"
          repeat: 1
      cec-commands:
        cec-home: "40:44"
```

Notes:

- If `ir-codes` does not include a profile key, the gateway should resolve by `profileKey` only.
- If `cec-commands` does not include an action, the gateway should resolve by `actionKey` only.
- For production secrets, prefer environment overrides or your secret manager rather than committing real values.

## Run in Real Mode

Option A: Use the included script:

```powershell
.\run-real.cmd
```

Option B: Run the jar with a profile:

```powershell
set SPRING_PROFILES_ACTIVE=real
java -jar target\adaptive-tv-remote-0.1.0.jar
```

macOS/Linux:

```bash
SPRING_PROFILES_ACTIVE=real java -jar target/adaptive-tv-remote-0.1.0.jar
```

## Verify

- Open `http://<host>:8080` in a browser
- Check integrations summary at `http://<host>:8080/api/remote/integrations`
- Run the demo flow and send a `HOME` or `VOLUME_UP` command

## Data Storage

By default, the service uses a local H2 database. For production, configure an external database and set:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## Troubleshooting

- If you see `real integration is disabled`, verify `remote.integration.default-mode` and `adapter-modes`.
- If Sony commands fail, confirm the TV has IP control enabled and the PSK matches.
- If IR commands fail, verify `hub-id`, `auth-token`, and the `ir-codes` profile or gateway code library.

## Error Codes

| Code | HTTP | Meaning |
| --- | --- | --- |
| `integration.disabled` | 503 | Integration is disabled by configuration. |
| `integration.config.missing` | 500 | Required endpoint/credential is missing. |
| `integration.transport.failure` | 502 | Upstream request failed (non-timeout). |
| `integration.transport.timeout` | 502 | Upstream request timed out. |
