# Adaptive TV Remote

Adaptive TV Remote is a Spring Boot + PWA prototype for smartphone-based TV control that automatically routes commands across LAN, IR gateway, and HDMI-CEC gateway paths.

## Highlights

- Adaptive routing across `LAN_DIRECT`, `IR_GATEWAY`, and `HDMI_CEC_GATEWAY`
- Persistent catalog for households, rooms, devices, pairings, and onboarding sessions
- Candidate discovery flow with suggestions, adoption, dismissal, reopening, and retry actions
- Brand onboarding first versions for Samsung, LG, and Sony
- Mobile-friendly PWA with a guided onboarding workspace and remote control surface

## Quickstart

```powershell
mvnw.cmd -q spring-boot:run
```

Open `http://localhost:8080`.

## Demo Flow

1. Click `Find candidates`
2. Select a discovered candidate
3. Review suggested paths in the guided setup panel
4. Keep `Auto-create pairings` and `Auto-start onboarding` enabled
5. Click `Adopt Candidate`
6. Review onboarding state and send a command

## Docs

- `docs/README.md`
- `docs/API-OVERVIEW.md`
- `docs/DEMO-GUIDE.md`
- `docs/CAPABILITIES-AND-LIMITATIONS.md`
- `docs/INTEGRATIONS.md`
- `docs/NEXT-DEVELOPMENT-PLAN.md`
- `docs/GITHUB-METADATA.md`

## Main APIs

- `GET /api/remote/households`
- `GET /api/remote/rooms`
- `GET /api/remote/discovery/candidates`
- `POST /api/remote/discovery/candidates/scan`
- `GET /api/remote/discovery/candidates/{candidateId}/pairing-suggestions`
- `POST /api/remote/discovery/candidates/{candidateId}/adopt`
- `POST /api/remote/discovery/candidates/{candidateId}/dismiss`
- `POST /api/remote/discovery/candidates/{candidateId}/reopen`
- `GET /api/remote/devices`
- `GET /api/remote/devices/{deviceId}`
- `POST /api/remote/devices/register`
- `GET /api/remote/devices/{deviceId}/pairings`
- `GET /api/remote/devices/{deviceId}/onboarding/sessions`
- `GET /api/remote/devices/{deviceId}/onboarding/status`
- `POST /api/remote/devices/{deviceId}/onboarding/retry`
- `POST /api/remote/pairings`
- `PATCH /api/remote/pairings/{pairingId}`
- `DELETE /api/remote/pairings/{pairingId}`
- `POST /api/remote/discovery/scan`
- `POST /api/remote/devices/{deviceId}/commands`
- `GET /api/remote/executions`
- `GET /api/remote/adapters`
- `GET /api/remote/integrations`
- `GET /api/remote/profile`

## Integration Modes

- Default protocol clients run in `mock`
- Individual adapters can switch to `real` first-version request flows
- Brand adapters currently include `samsung-lan`, `lg-lan`, and `sony-lan`
- Gateway adapters currently include `generic-ir` and `generic-hdmi-cec`

## Repository Notes

- CI workflow: `.github/workflows/ci.yml`
- Release workflow: `.github/workflows/release.yml`
- Contribution guide: `CONTRIBUTING.md`
- Security note: `SECURITY.md`
