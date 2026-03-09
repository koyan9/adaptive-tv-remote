# Demo Guide

This guide helps demonstrate the current repository in a predictable way.

## Demo Goal

Show that the project can:

- discover candidate TVs and projectors
- suggest control paths automatically
- adopt a candidate into the managed catalog
- create or reuse pairings
- trigger brand onboarding
- route commands using adaptive control paths

## Setup

From the project root:

```powershell
mvnw.cmd -q spring-boot:run
```

Then open:

- `http://localhost:8080`

## Recommended Demo Script

### 1. Show the managed device catalog

- Point out the current rooms and managed TVs
- Select a device and show the onboarding panel and routing badge

### 2. Scan candidate devices

- Click `Find candidates`
- Show the candidate list in the left column
- Explain the difference between `Discovered`, `Adopted`, and `Dismissed`

### 3. Show suggestions

- Select `candidate-playroom-projector`
- Point out suggested `HDMI_CEC_GATEWAY` and gateway-backed setup guidance

### 4. Adopt a candidate

- Select `candidate-kitchen-lg`, `candidate-loft-samsung`, or `candidate-study-sony`
- Keep `Auto-create pairings` and `Auto-start onboarding` enabled
- Click `Adopt Candidate`
- Show the success banner and that the device appears in the managed catalog

### 5. Show onboarding state

- With the adopted device selected, point out the `Onboarding` panel
- Explain provider, session count, credential preview, and retry action

### 6. Show adaptive routing

- Send `HOME` or `VOLUME_UP`
- Point out the route badge and recent execution log
- Explain how the routing layer chooses LAN, IR, or HDMI-CEC

## Good Demo Candidates

- `candidate-loft-samsung` — good for Samsung onboarding and LAN routing
- `candidate-kitchen-lg` — good for LG onboarding and LAN routing
- `candidate-study-sony` — good for Sony onboarding and LAN routing
- `candidate-playroom-projector` — good for gateway-oriented onboarding and HDMI-CEC suggestions

## Demo Caveats

- Real integrations are still first-version request flows and onboarding skeletons
- The project is excellent for demonstrating architecture and workflow, but not yet for claiming broad production compatibility
