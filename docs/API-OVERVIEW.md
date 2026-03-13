# API Overview

This document summarizes the current REST API surface of `Adaptive TV Remote`.

## Core Control

- `GET /api/remote/devices` — list managed television devices
- `GET /api/remote/devices/{deviceId}` — fetch one managed device
- `POST /api/remote/devices/{deviceId}/commands` — send a remote command through the adaptive routing layer (optional `networkName` for same-Wi-Fi enforcement)
- `GET /api/remote/executions` — list recent command executions

Example command request body:

```json
{
  "command": "HOME",
  "networkName": "Koyan Home Mesh"
}
```

## Discovery Guidance

- `POST /api/remote/discovery/scan` — scan the home and return currently visible managed devices
- `GET /api/remote/discovery/candidates` — list candidate devices, optionally filtered by status
- `POST /api/remote/discovery/candidates/scan` — refresh and persist candidate devices
- `GET /api/remote/discovery/candidates/{candidateId}/pairing-suggestions` — show recommended control-path suggestions for a candidate device (optional `networkName` to filter LAN suggestions)
- `POST /api/remote/discovery/candidates/{candidateId}/adopt` — adopt a candidate into the managed device catalog
- `POST /api/remote/discovery/candidates/{candidateId}/dismiss` — dismiss a noisy candidate from the active onboarding flow
- `POST /api/remote/discovery/candidates/{candidateId}/reopen` — bring a dismissed candidate back into the active onboarding flow

Example pairing suggestions request:

```
GET /api/remote/discovery/candidates/candidate-kitchen-lg/pairing-suggestions?networkName=Koyan%20Home%20Mesh
```

## Device Catalog Management

- `GET /api/remote/households` — list households with room and device counts
- `GET /api/remote/rooms` — list rooms, optionally filtered by household
- `POST /api/remote/devices/register` — register a managed device directly into the persisted catalog

## Pairing Management

- `GET /api/remote/devices/{deviceId}/pairings` — list pairings associated with a managed device
- `POST /api/remote/devices/{deviceId}/pairings/repair` — rebuild missing pairings using known device/gateway info
- `POST /api/remote/pairings` — create a pairing for a managed device
- `PATCH /api/remote/pairings/{pairingId}` — update pairing status or metadata
- `DELETE /api/remote/pairings/{pairingId}` — revoke an existing pairing

## Brand Onboarding

- `GET /api/remote/devices/{deviceId}/onboarding/samsung-handshakes` — list Samsung-specific handshake history
- `GET /api/remote/devices/{deviceId}/onboarding/sessions` — list brand onboarding sessions, optionally filtered by brand
- `GET /api/remote/devices/{deviceId}/onboarding/status` — summarize the latest onboarding state for a device (includes `latestFailureReason` when status is FAILED)
- `POST /api/remote/devices/{deviceId}/onboarding/retry` — trigger a new onboarding attempt for a supported brand

## Introspection

- `GET /api/remote/adapters` — list registered brand adapters
- `GET /api/remote/integrations` — list protocol client and integration configuration summary
- `GET /api/remote/health/integrations` — health summary of adapters (mode, config completeness, client availability)
- `GET /api/remote/profile` — list high-level project metadata and strategy summary

## Current Response Style

- Errors return RFC 7807-style `ProblemDetail` responses
- Routing failures may include `reason` (e.g. `WIFI_MISMATCH`) and `attemptedPaths`
- Most APIs return JSON objects or arrays directly
- Candidate and onboarding APIs are designed for the built-in PWA guide flow first
