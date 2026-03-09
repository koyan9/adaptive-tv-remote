# Capabilities and Limitations

## What Works Today

### Control Routing

- adaptive route selection across `LAN_DIRECT`, `IR_GATEWAY`, and `HDMI_CEC_GATEWAY`
- command execution history for recent actions
- persisted device catalog rather than in-memory-only state

### Device and Pairing Management

- household and room listing
- managed device registration
- pairing creation, revocation, reactivation, and routing-aware state handling

### Candidate Discovery

- candidate scanning and persistence
- suggestion generation for candidate control paths
- candidate adoption into the managed catalog
- candidate dismissal and reopening

### Brand Onboarding

- Samsung onboarding first version
- LG onboarding first version
- Sony onboarding first version
- unified onboarding session listing and per-device onboarding status summary

### Frontend/PWA

- remote control interface for managed devices
- onboarding status panel
- candidate guidance panel with step-based flow
- retry and recovery actions for onboarding and candidate flow

## Current Limitations

### Integration Depth

- so-called `real` integrations are first-version protocol request flows, not production-hardened vendor integrations
- token or credential negotiation is still partial and intentionally simplified
- no end-to-end validation against a real hardware fleet is included in this repository

### Data Model Scope

- current persistence is focused on a single demo-friendly local deployment
- no user accounts, authentication, or authorization model is present yet
- no audit trail beyond recent command history and onboarding session records

### Frontend Scope

- the built-in PWA is a demo/operator UI, not a polished consumer-ready mobile product
- the onboarding wizard is strong enough for demos, but not yet equivalent to a full production onboarding experience

### Operational Readiness

- no production secret management
- no packaged release artifact strategy beyond the current repository workflows
- no metrics dashboard or external observability stack integration yet

## Best Current Use Cases

- architecture demonstrations
- workflow demonstrations for adaptive routing and onboarding
- rapid prototyping for brand or gateway integrations
- design exploration for a future real product
