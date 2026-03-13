const state = {
  devices: [],
  candidates: [],
  candidateFilter: "DISCOVERED",
  selectedDeviceId: null,
  selectedCandidateId: null,
  candidateSuggestions: [],
  executions: [],
  onboardingStatus: null,
  adoptionSuccess: null,
  guideError: null,
  networkName: null,
  integrationIssuesOnly: false,
  integrationPathFilter: "ALL",
  integrationSortIssuesFirst: true,
  pairingsActiveOnly: true,
  integrationAutoRefresh: false,
  integrationRefreshTimer: null,
  integrationCountdownTimer: null,
  integrationCountdownRemaining: 0
};

const els = {
  deviceList: document.getElementById("deviceList"),
  candidateList: document.getElementById("candidateList"),
  deviceTemplate: document.getElementById("deviceCardTemplate"),
  candidateTemplate: document.getElementById("candidateCardTemplate"),
  logTemplate: document.getElementById("logItemTemplate"),
  activityLog: document.getElementById("activityLog"),
  scanButton: document.getElementById("scanButton"),
  scanCandidatesButton: document.getElementById("scanCandidatesButton"),
  refreshSuggestionsButton: document.getElementById("refreshSuggestionsButton"),
  retryOnboardingButton: document.getElementById("retryOnboardingButton"),
  openAdoptedDeviceButton: document.getElementById("openAdoptedDeviceButton"),
  dismissCandidateButton: document.getElementById("dismissCandidateButton"),
  reopenCandidateButton: document.getElementById("reopenCandidateButton"),
  networkBadge: document.getElementById("networkBadge"),
  candidateBadge: document.getElementById("candidateBadge"),
  deviceName: document.getElementById("deviceName"),
  deviceMeta: document.getElementById("deviceMeta"),
  routeBadge: document.getElementById("routeBadge"),
  roomValue: document.getElementById("roomValue"),
  brandValue: document.getElementById("brandValue"),
  pathsValue: document.getElementById("pathsValue"),
  capabilities: document.getElementById("capabilities"),
  onboardingState: document.getElementById("onboardingState"),
  onboardingProvider: document.getElementById("onboardingProvider"),
  onboardingSessionCount: document.getElementById("onboardingSessionCount"),
  onboardingCredential: document.getElementById("onboardingCredential"),
  onboardingFailureReason: document.getElementById("onboardingFailureReason"),
  onboardingFailureBanner: document.getElementById("onboardingFailureBanner"),
  onboardingFailureTitle: document.getElementById("onboardingFailureTitle"),
  onboardingFailureMessage: document.getElementById("onboardingFailureMessage"),
  onboardingFailureAction: document.getElementById("onboardingFailureAction"),
  onboardingDetail: document.getElementById("onboardingDetail"),
  repairPairingsButton: document.getElementById("repairPairingsButton"),
  openPairingsButton: document.getElementById("openPairingsButton"),
  pairingsPanel: document.getElementById("pairingsPanel"),
  pairingsList: document.getElementById("pairingsList"),
  refreshPairingsButton: document.getElementById("refreshPairingsButton"),
  pairingsActiveOnlyButton: document.getElementById("pairingsActiveOnlyButton"),
  pairingsFilterState: document.getElementById("pairingsFilterState"),
  copyPairingsButton: document.getElementById("copyPairingsButton"),
  exportPairingsButton: document.getElementById("exportPairingsButton"),
  pairingsExportError: document.getElementById("pairingsExportError"),
  pairingsExportRetry: document.getElementById("pairingsExportRetry"),
  candidateState: document.getElementById("candidateState"),
  candidateName: document.getElementById("candidateName"),
  candidateMeta: document.getElementById("candidateMeta"),
  candidateSummary: document.getElementById("candidateSummary"),
  candidateSuggestions: document.getElementById("candidateSuggestions"),
  adoptCandidateButton: document.getElementById("adoptCandidateButton"),
  guideSuccessBanner: document.getElementById("guideSuccessBanner"),
  guideSuccessTitle: document.getElementById("guideSuccessTitle"),
  guideSuccessMessage: document.getElementById("guideSuccessMessage"),
  guideErrorBanner: document.getElementById("guideErrorBanner"),
  guideErrorTitle: document.getElementById("guideErrorTitle"),
  guideErrorMessage: document.getElementById("guideErrorMessage"),
  routeTipBanner: document.getElementById("routeTipBanner"),
  routeTipTitle: document.getElementById("routeTipTitle"),
  routeTipMessage: document.getElementById("routeTipMessage"),
  routeTipAction: document.getElementById("routeTipAction"),
  routeTipChips: document.getElementById("routeTipChips"),
  integrationHealthList: document.getElementById("integrationHealthList"),
  refreshIntegrationsButton: document.getElementById("refreshIntegrationsButton"),
  integrationReadyCount: document.getElementById("integrationReadyCount"),
  integrationIssueCount: document.getElementById("integrationIssueCount"),
  integrationFilterButton: document.getElementById("integrationFilterButton"),
  integrationFilterState: document.getElementById("integrationFilterState"),
  integrationSortButton: document.getElementById("integrationSortButton"),
  integrationLastChecked: document.getElementById("integrationLastChecked"),
  integrationCountdown: document.getElementById("integrationCountdown"),
  integrationAutoRefreshButton: document.getElementById("integrationAutoRefreshButton"),
  guideAutoPairings: document.getElementById("guideAutoPairings"),
  guideAutoOnboarding: document.getElementById("guideAutoOnboarding"),
  wizardSteps: [...document.querySelectorAll(".wizard-step")],
  candidateFilterDiscovered: document.getElementById("candidateFilterDiscovered"),
  candidateFilterAdopted: document.getElementById("candidateFilterAdopted"),
  candidateFilterDismissed: document.getElementById("candidateFilterDismissed"),
  candidateFilterAll: document.getElementById("candidateFilterAll")
};

const commandButtons = [...document.querySelectorAll("[data-command]")];

async function api(path, options = {}) {
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    const problem = await response.json().catch(() => ({ detail: "Request failed." }));
    const error = new Error(problem.detail || "Request failed.");
    error.problem = problem;
    error.status = response.status;
    throw error;
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

function getSelectedDevice() {
  return state.devices.find(device => device.id === state.selectedDeviceId) || null;
}

function getSelectedCandidate() {
  return state.candidates.find(candidate => candidate.id === state.selectedCandidateId) || null;
}

function visibleCandidates() {
  if (state.candidateFilter === "ALL") {
    return state.candidates;
  }
  return state.candidates.filter(candidate => candidate.status === state.candidateFilter);
}

function wizardStep() {
  const candidate = getSelectedCandidate();
  if (!state.candidates.length) return "scan";
  if (!candidate) return "review";
  if (candidate.status === "ADOPTED") return "adopted";
  if (state.candidateSuggestions.length > 0) return "suggestions";
  return "review";
}

function syncCandidateSelection() {
  const candidates = visibleCandidates();
  if (!candidates.find(candidate => candidate.id === state.selectedCandidateId)) {
    state.selectedCandidateId = candidates.length ? candidates[0].id : null;
  }
}

async function loadDevices() {
  state.devices = await api("/api/remote/devices");
  if (!state.selectedDeviceId && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  await refreshSelectedDevice();
}

async function loadCandidates() {
  state.candidates = await api("/api/remote/discovery/candidates");
  syncCandidateSelection();
  renderCandidates();
  await refreshSelectedCandidate();
}

async function scanHome() {
  els.networkBadge.textContent = "Scanning…";
  const result = await api("/api/remote/discovery/scan", { method: "POST" });
  state.networkName = result.networkName;
  els.networkBadge.textContent = `${result.networkName} · ${result.devices.length} devices`;
  state.devices = result.devices.filter(device => device.deviceType !== "GATEWAY");
  if (!state.devices.find(device => device.id === state.selectedDeviceId) && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  await refreshSelectedDevice();
}

async function scanCandidates() {
  state.guideError = null;
  state.adoptionSuccess = null;
  els.candidateBadge.textContent = "Scanning…";
  state.candidates = await api("/api/remote/discovery/candidates/scan", { method: "POST" });
  syncCandidateSelection();
  renderCandidates();
  await refreshSelectedCandidate();
}

async function loadExecutions() {
  state.executions = await api("/api/remote/executions");
  renderExecutions();
}

async function loadOnboardingStatus() {
  const device = getSelectedDevice();
  if (!device) {
    state.onboardingStatus = null;
    return;
  }

  try {
    state.onboardingStatus = await api(`/api/remote/devices/${device.id}/onboarding/status`);
  } catch {
    state.onboardingStatus = null;
  }
}

async function loadCandidateSuggestions() {
  const candidate = getSelectedCandidate();
  if (!candidate || candidate.status !== "DISCOVERED") {
    state.candidateSuggestions = [];
    return;
  }

  try {
    const networkParam = state.networkName ? `?networkName=${encodeURIComponent(state.networkName)}` : "";
    state.candidateSuggestions = await api(`/api/remote/discovery/candidates/${candidate.id}/pairing-suggestions${networkParam}`);
  } catch {
    state.candidateSuggestions = [];
  }
}

async function refreshSelectedDevice() {
  renderSelectedDeviceBase();
  await loadOnboardingStatus();
  renderOnboardingStatus();
}

async function refreshSelectedCandidate() {
  renderCandidateGuideBase();
  await loadCandidateSuggestions();
  renderCandidateGuide();
  renderWizardSteps();
}

function renderDevices() {
  els.deviceList.innerHTML = "";
  state.devices.forEach(device => {
    const fragment = els.deviceTemplate.content.cloneNode(true);
    const button = fragment.querySelector("button");
    button.classList.toggle("is-selected", device.id === state.selectedDeviceId);
    fragment.querySelector(".device-room").textContent = device.room;
    fragment.querySelector(".device-name").textContent = device.displayName;
    fragment.querySelector(".device-model").textContent = `${device.brand} ${device.model}`;
    const stateEl = fragment.querySelector(".device-state");
    stateEl.textContent = device.online ? "Wi-Fi online" : "Waiting for gateway fallback";
    stateEl.classList.toggle("is-offline", !device.online);

    button.addEventListener("click", async () => {
      state.selectedDeviceId = device.id;
      renderDevices();
      await refreshSelectedDevice();
    });

    els.deviceList.appendChild(fragment);
  });
}

function renderCandidates() {
  els.candidateList.innerHTML = "";
  const candidates = visibleCandidates();

  if (candidates.length === 0) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = state.candidateFilter === "DISCOVERED"
      ? "No discovered candidates. Use Find candidates to scan the home."
      : state.candidateFilter === "ADOPTED"
        ? "No adopted candidates yet."
        : state.candidateFilter === "DISMISSED"
          ? "No dismissed candidates."
          : "No candidates available.";
    els.candidateList.appendChild(empty);
    els.candidateBadge.textContent = "No candidates";
    return;
  }

  els.candidateBadge.textContent = `${candidates.length} candidates`;
  candidates.forEach(candidate => {
    const fragment = els.candidateTemplate.content.cloneNode(true);
    const button = fragment.querySelector("button");
    button.classList.toggle("is-selected", candidate.id === state.selectedCandidateId);
    fragment.querySelector(".candidate-room").textContent = candidate.roomName;
    fragment.querySelector(".candidate-name").textContent = candidate.displayName;
    fragment.querySelector(".candidate-model").textContent = `${candidate.brand} ${candidate.model}`;
    const wifiBadge = fragment.querySelector(".candidate-wifi");
    if (wifiBadge) {
      wifiBadge.classList.toggle("hidden", !candidate.sameWifiRequired);
    }

    const stateEl = fragment.querySelector(".candidate-state");
    stateEl.textContent = candidate.status === "ADOPTED"
      ? `Adopted → ${candidate.adoptedDeviceId}`
      : candidate.status === "DISMISSED"
        ? "Dismissed"
        : candidate.online
          ? "Ready for onboarding"
          : "Needs gateway guidance";
    stateEl.classList.toggle("is-offline", !candidate.online && candidate.status === "DISCOVERED");

    button.addEventListener("click", async () => {
      state.selectedCandidateId = candidate.id;
      state.guideError = null;
      renderCandidates();
      await refreshSelectedCandidate();
    });

    els.candidateList.appendChild(fragment);
  });
}

function renderSelectedDeviceBase() {
  const device = getSelectedDevice();
  if (!device) {
    els.deviceName.textContent = "Select a TV";
    els.deviceMeta.textContent = "The app will choose the best available route automatically.";
    els.routeBadge.textContent = "Idle";
    els.roomValue.textContent = "—";
    els.brandValue.textContent = "—";
    els.pathsValue.textContent = "—";
    els.capabilities.innerHTML = "";
    commandButtons.forEach(button => button.disabled = true);
    els.repairPairingsButton.disabled = true;
    if (els.openPairingsButton) {
      els.openPairingsButton.disabled = true;
    }
    if (els.pairingsPanel) {
      els.pairingsPanel.classList.add("hidden");
    }
    renderOnboardingStatus();
    return;
  }

  els.deviceName.textContent = device.displayName;
  els.deviceMeta.textContent = device.profile.notes;
  els.routeBadge.textContent = device.online ? "LAN preferred" : "Gateway fallback";
  els.roomValue.textContent = device.room;
  els.brandValue.textContent = `${device.brand} ${device.model}`;
  els.pathsValue.textContent = [...device.availablePaths].map(prettyLabel).join(" · ");

  els.capabilities.innerHTML = "";
  device.capability.supportedCommands.forEach(command => {
    const chip = document.createElement("span");
    chip.textContent = prettyCommand(command);
    els.capabilities.appendChild(chip);
  });

  const supported = new Set(device.capability.supportedCommands);
  commandButtons.forEach(button => {
    button.disabled = !supported.has(button.dataset.command);
  });
  els.repairPairingsButton.disabled = false;
  if (els.openPairingsButton) {
    els.openPairingsButton.disabled = false;
  }
}

function renderOnboardingStatus() {
  const device = getSelectedDevice();
  const status = state.onboardingStatus;

  if (!device) {
    els.onboardingState.textContent = "No session";
    els.onboardingProvider.textContent = "—";
    els.onboardingSessionCount.textContent = "0";
    els.onboardingCredential.textContent = "—";
    els.onboardingFailureReason.textContent = "—";
    els.onboardingFailureBanner.classList.add("hidden");
    if (els.onboardingFailureAction) {
      els.onboardingFailureAction.classList.add("hidden");
    }
    els.onboardingDetail.textContent = "Select a device to view onboarding status.";
    els.retryOnboardingButton.disabled = true;
    return;
  }

  if (!status || !status.onboardingSupported) {
    els.onboardingState.textContent = "Not required";
    els.onboardingProvider.textContent = "—";
    els.onboardingSessionCount.textContent = "0";
    els.onboardingCredential.textContent = "—";
    els.onboardingFailureReason.textContent = "—";
    els.onboardingFailureBanner.classList.add("hidden");
    if (els.onboardingFailureAction) {
      els.onboardingFailureAction.classList.add("hidden");
    }
    els.onboardingDetail.textContent = "This device does not require brand onboarding for its current integration path.";
    els.retryOnboardingButton.disabled = true;
    return;
  }

  els.onboardingState.textContent = status.latestStatus || "No session";
  els.onboardingProvider.textContent = status.latestProviderId || status.brand;
  els.onboardingSessionCount.textContent = String(status.sessionCount ?? 0);
  els.onboardingCredential.textContent = status.negotiatedCredentialPresent ? (status.credentialPreview || "Stored") : "Not negotiated";
  els.onboardingFailureReason.textContent = status.latestFailureReason || "—";
  const failureReason = status.latestFailureReason;
  if (status.latestStatus === "FAILED" && failureReason) {
    els.onboardingFailureBanner.classList.remove("hidden");
    els.onboardingFailureTitle.textContent = "Onboarding failed";
    els.onboardingFailureMessage.textContent = onboardingFailureMessage(failureReason);
    if (els.onboardingFailureAction) {
      els.onboardingFailureAction.classList.remove("hidden");
    }
  } else {
    els.onboardingFailureBanner.classList.add("hidden");
    if (els.onboardingFailureAction) {
      els.onboardingFailureAction.classList.add("hidden");
    }
  }
  els.onboardingDetail.textContent = status.latestDetail || "No onboarding session has been recorded yet.";
  els.retryOnboardingButton.disabled = false;
}

function onboardingFailureMessage(reason) {
  switch (reason) {
    case "AUTH_FAILURE":
      return "Authentication failed. Confirm the device token or pre-shared key and retry.";
    case "TIMEOUT":
      return "The onboarding request timed out. Ensure the TV is online and retry.";
    case "PROTOCOL_UNSUPPORTED":
      return "This onboarding path is not available. Verify integration settings.";
    case "UNKNOWN":
    default:
      return "Onboarding failed. Check integration settings and retry.";
  }
}

function renderCandidateGuideBase() {
  const candidate = getSelectedCandidate();
  els.guideSuccessBanner.classList.toggle("hidden", !state.adoptionSuccess);
  els.guideErrorBanner.classList.toggle("hidden", !state.guideError);
  els.routeTipBanner.classList.toggle("hidden", true);
  if (els.routeTipAction) {
    els.routeTipAction.classList.add("hidden");
  }
  if (els.routeTipChips) {
    els.routeTipChips.classList.add("hidden");
    els.routeTipChips.innerHTML = "";
  }

  if (state.adoptionSuccess) {
    els.guideSuccessTitle.textContent = `${state.adoptionSuccess.displayName} adopted`;
    els.guideSuccessMessage.textContent = `${state.adoptionSuccess.displayName} is now part of the device catalog as ${state.adoptionSuccess.deviceId}.`;
  }

  if (state.guideError) {
    els.guideErrorTitle.textContent = state.guideError.title;
    els.guideErrorMessage.textContent = state.guideError.message;
  }
  if (els.routeTipBanner.classList.contains("hidden")) {
    els.routeTipTitle.textContent = "Routing tip";
    els.routeTipMessage.textContent = "Resolve pairing or Wi-Fi issues to continue.";
  }

  if (!candidate) {
    els.candidateState.textContent = "No candidate";
    els.candidateName.textContent = "Select a candidate";
    els.candidateMeta.textContent = "Scan the home to discover candidate TVs and projectors that can be adopted into the device catalog.";
    els.candidateSummary.textContent = "No candidate selected.";
    els.candidateSuggestions.innerHTML = "";
    els.adoptCandidateButton.disabled = true;
    els.refreshSuggestionsButton.disabled = true;
    els.dismissCandidateButton.disabled = true;
    els.reopenCandidateButton.disabled = true;
    els.openAdoptedDeviceButton.disabled = true;
    return;
  }

  els.candidateState.textContent = candidate.status;
  els.candidateName.textContent = candidate.displayName;
  els.candidateMeta.textContent = `${candidate.brand} ${candidate.model} · ${candidate.roomName} · ${candidate.discoverySource}`;
  els.candidateSummary.textContent = candidate.online
    ? "This candidate is online and ready for direct onboarding or adaptive fallback."
    : "This candidate is offline and may need gateway-assisted onboarding.";
  els.adoptCandidateButton.disabled = candidate.status !== "DISCOVERED";
  els.refreshSuggestionsButton.disabled = candidate.status !== "DISCOVERED";
  els.dismissCandidateButton.disabled = candidate.status !== "DISCOVERED";
  els.reopenCandidateButton.disabled = candidate.status !== "DISMISSED";
  els.openAdoptedDeviceButton.disabled = !(candidate.status === "ADOPTED" && candidate.adoptedDeviceId);
}

function renderCandidateGuide() {
  const candidate = getSelectedCandidate();
  els.candidateSuggestions.innerHTML = "";

  if (!candidate) {
    return;
  }

  if (candidate.status !== "DISCOVERED") {
    const note = document.createElement("p");
    note.className = "muted";
    note.textContent = candidate.status === "DISMISSED"
      ? "This candidate is dismissed. Reopen it to continue onboarding."
      : "This candidate has already been adopted. Use Open Adopted Device to jump into the remote workspace.";
    els.candidateSuggestions.appendChild(note);
    return;
  }

  const wifiNotice = candidate.sameWifiRequired
    ? `Same Wi-Fi required for direct control.${state.networkName ? ` Current network: ${state.networkName}.` : ""}`
    : null;

  if (state.candidateSuggestions.length === 0) {
    const empty = document.createElement("p");
    empty.className = "muted";
    if (wifiNotice) {
      empty.textContent = wifiNotice;
    } else {
      empty.textContent = wizardStep() === "review"
        ? "Refresh suggestions to evaluate the best path for this candidate."
        : "No pairing suggestions available for this candidate yet.";
    }
    els.candidateSuggestions.appendChild(empty);
    return;
  }

  if (wifiNotice) {
    const note = document.createElement("p");
    note.className = "muted";
    note.textContent = wifiNotice;
    els.candidateSuggestions.appendChild(note);
  }

  state.candidateSuggestions.forEach(suggestion => {
    const card = document.createElement("article");
    card.className = "suggestion-card";
    card.innerHTML = `
      <div class="suggestion-top">
        <strong>${prettyLabel(suggestion.controlPath)}</strong>
        <span class="badge muted">${suggestion.autoSelectable ? "Recommended" : "Manual"}</span>
      </div>
      <p class="muted">${suggestion.rationale}</p>
      <div class="suggestion-meta">${suggestion.gatewayDeviceName || "Direct LAN onboarding"}</div>
    `;
    els.candidateSuggestions.appendChild(card);
  });
}

function renderWizardSteps() {
  const current = wizardStep();
  const order = ["scan", "review", "suggestions", "adopted"];
  const currentIndex = order.indexOf(current);
  els.wizardSteps.forEach(step => {
    const stepIndex = order.indexOf(step.dataset.step);
    step.classList.toggle("is-active", step.dataset.step === current);
    step.classList.toggle("is-complete", stepIndex < currentIndex);
  });
}

async function refreshSuggestions() {
  state.guideError = null;
  await loadCandidateSuggestions();
  renderCandidateGuide();
  renderWizardSteps();
}

async function adoptSelectedCandidate() {
  const candidate = getSelectedCandidate();
  if (!candidate || candidate.status !== "DISCOVERED") {
    return;
  }

  const payload = {
    roomName: candidate.roomName,
    autoCreatePairings: els.guideAutoPairings.checked,
    autoStartBrandOnboarding: els.guideAutoOnboarding.checked
  };

  try {
    state.guideError = null;
    state.adoptionSuccess = null;
    renderCandidateGuideBase();
    els.adoptCandidateButton.disabled = true;
    els.candidateState.textContent = "Adopting…";
    const adopted = await api(`/api/remote/discovery/candidates/${candidate.id}/adopt`, {
      method: "POST",
      body: JSON.stringify(payload)
    });

    state.adoptionSuccess = { displayName: adopted.displayName, deviceId: adopted.id };
    await Promise.all([loadDevices(), loadCandidates(), loadExecutions()]);
    state.selectedDeviceId = adopted.id;
    renderDevices();
    await refreshSelectedDevice();
    await refreshSelectedCandidate();
  } catch (error) {
    state.guideError = {
      title: "Adoption failed",
      message: error.message
    };
    els.adoptCandidateButton.disabled = false;
    renderCandidateGuideBase();
  }
}

async function changeCandidateStatus(action) {
  const candidate = getSelectedCandidate();
  if (!candidate) {
    return;
  }

  try {
    state.guideError = null;
    state.adoptionSuccess = null;
    await api(`/api/remote/discovery/candidates/${candidate.id}/${action}`, { method: "POST" });
    await loadCandidates();
    await refreshSelectedCandidate();
  } catch (error) {
    state.guideError = {
      title: action === "dismiss" ? "Dismiss failed" : "Reopen failed",
      message: error.message
    };
    renderCandidateGuideBase();
  }
}

async function retryOnboarding() {
  const device = getSelectedDevice();
  if (!device || !state.onboardingStatus?.onboardingSupported) {
    return;
  }

  try {
    els.retryOnboardingButton.disabled = true;
    state.guideError = null;
    await api(`/api/remote/devices/${device.id}/onboarding/retry?brand=${encodeURIComponent(device.brand)}`, {
      method: "POST"
    });
    await loadOnboardingStatus();
    renderOnboardingStatus();
    state.adoptionSuccess = {
      displayName: device.displayName,
      deviceId: device.id
    };
    renderCandidateGuideBase();
  } catch (error) {
    state.guideError = {
      title: "Onboarding retry failed",
      message: error.message
    };
    renderCandidateGuideBase();
  } finally {
    els.retryOnboardingButton.disabled = false;
  }
}

function openAdoptedDevice() {
  const candidate = getSelectedCandidate();
  if (!candidate?.adoptedDeviceId) {
    return;
  }
  state.selectedDeviceId = candidate.adoptedDeviceId;
  renderDevices();
  refreshSelectedDevice();
}

async function repairPairings() {
  const device = getSelectedDevice();
  if (!device) {
    return;
  }

  try {
    els.repairPairingsButton.disabled = true;
    els.routeTipBanner.classList.add("hidden");
    if (els.routeTipAction) {
      els.routeTipAction.classList.add("hidden");
    }
    if (els.routeTipChips) {
      els.routeTipChips.classList.add("hidden");
      els.routeTipChips.innerHTML = "";
    }
    const repaired = await api(`/api/remote/devices/${device.id}/pairings/repair`, { method: "POST" });
    const repairedCount = Array.isArray(repaired) ? repaired.length : 0;
    els.routeBadge.textContent = "Pairings repaired";
    const message = repairedCount > 0
      ? `Repaired ${repairedCount} pairing${repairedCount === 1 ? "" : "s"} for ${device.displayName}.`
      : "No missing pairings were found.";
    els.routeTipTitle.textContent = "Pairings repaired";
    els.routeTipMessage.textContent = message;
    els.routeTipBanner.classList.remove("hidden");
    if (els.routeTipAction) {
      els.routeTipAction.classList.add("hidden");
    }
    if (els.routeTipChips) {
      els.routeTipChips.classList.add("hidden");
      els.routeTipChips.innerHTML = "";
    }
    els.guideError = null;
    els.adoptionSuccess = { displayName: device.displayName, deviceId: device.id };
    renderCandidateGuideBase();
    loadIntegrationHealth();
    const entry = {
      command: "PAIRING_REPAIR",
      deviceName: device.displayName,
      route: "PAIRING",
      executedAt: new Date().toISOString(),
      message
    };
    state.executions = [entry, ...state.executions].slice(0, 12);
    renderExecutions();
    if (els.pairingsPanel && !els.pairingsPanel.classList.contains("hidden")) {
      loadPairings();
    }
  } catch (error) {
    els.routeBadge.textContent = "Repair failed";
    els.routeTipTitle.textContent = "Repair failed";
    els.routeTipMessage.textContent = error.message;
    els.routeTipBanner.classList.remove("hidden");
    if (els.routeTipAction) {
      els.routeTipAction.classList.add("hidden");
    }
    if (els.routeTipChips) {
      els.routeTipChips.classList.add("hidden");
      els.routeTipChips.innerHTML = "";
    }
    els.guideError = {
      title: "Repair failed",
      message: `${error.message} Use the Pairings list for details.`
    };
    renderCandidateGuideBase();
    const entry = {
      command: "PAIRING_REPAIR",
      deviceName: device.displayName,
      route: "ERROR",
      executedAt: new Date().toISOString(),
      message: error.message
    };
    state.executions = [entry, ...state.executions].slice(0, 12);
    renderExecutions();
  } finally {
    els.repairPairingsButton.disabled = false;
  }
}

async function togglePairingsPanel() {
  if (!els.pairingsPanel || !els.pairingsList) {
    return;
  }
  const isHidden = els.pairingsPanel.classList.contains("hidden");
  if (isHidden) {
    updatePairingsFilterState();
    await loadPairings();
    els.pairingsPanel.classList.remove("hidden");
  } else {
    els.pairingsPanel.classList.add("hidden");
  }
}

function togglePairingsFilter() {
  state.pairingsActiveOnly = !state.pairingsActiveOnly;
  if (els.pairingsActiveOnlyButton) {
    els.pairingsActiveOnlyButton.textContent = state.pairingsActiveOnly ? "Active only" : "All pairings";
    els.pairingsActiveOnlyButton.classList.toggle("is-active", state.pairingsActiveOnly);
  }
  updatePairingsFilterState();
  if (els.pairingsPanel && !els.pairingsPanel.classList.contains("hidden")) {
    loadPairings();
  }
}

function updatePairingsFilterState() {
  if (!els.pairingsFilterState) {
    return;
  }
  const parts = [];
  parts.push(state.pairingsActiveOnly ? "Active" : "All");
  if (state.integrationPathFilter && state.integrationPathFilter !== "ALL") {
    parts.push(prettyLabel(state.integrationPathFilter));
  } else {
    parts.push("All");
  }
  els.pairingsFilterState.textContent = `Filter: ${parts.join(" + ")}`;
}

async function loadPairings() {
  const device = getSelectedDevice();
  if (!device || !els.pairingsList) {
    return [];
  }
  try {
    els.pairingsList.innerHTML = "";
    let pairings = await api(`/api/remote/devices/${device.id}/pairings`);
  if (state.pairingsActiveOnly) {
    pairings = pairings.filter(pairing => pairing.status === "ACTIVE");
  }
  if (state.integrationPathFilter && state.integrationPathFilter !== "ALL") {
    pairings = pairings.filter(pairing => pairing.controlPath === state.integrationPathFilter);
  }
  updatePairingsFilterState();
    if (!Array.isArray(pairings) || pairings.length === 0) {
      const empty = document.createElement("p");
      empty.className = "muted";
      if (state.pairingsActiveOnly && state.integrationPathFilter && state.integrationPathFilter !== "ALL") {
        empty.textContent = "No active pairings found for this path.";
      } else if (state.pairingsActiveOnly) {
        empty.textContent = "No active pairings found.";
      } else {
        empty.textContent = "No pairings found.";
      }
      els.pairingsList.appendChild(empty);
      return pairings;
    }
    pairings.forEach(pairing => {
      const card = document.createElement("div");
      card.className = "pairing-card";
      const gateway = pairing.gatewayDeviceName || pairing.gatewayDeviceId || "Direct";
      const notes = pairing.notes || "";
      const externalRef = pairing.externalReference || "";
      const missingConfig = Array.isArray(pairing.missingConfig) ? pairing.missingConfig : [];
      card.innerHTML = `
        <strong>${prettyLabel(pairing.controlPath)}</strong>
        <span>Status: ${pairing.status}</span>
        <span>Gateway: ${gateway}</span>
        ${notes ? `<span>Notes: ${notes}</span>` : ""}
        ${externalRef ? `<span>Ref: ${externalRef}</span>` : ""}
        <div class="pairing-actions">
          ${notes ? `<button data-copy="${escapeHtml(notes)}" type="button">Copy notes</button>` : ""}
          ${externalRef ? `<button data-copy="${escapeHtml(externalRef)}" type="button">Copy ref</button>` : ""}
        </div>
      `;
      const buttons = card.querySelectorAll("[data-copy]");
      buttons.forEach(button => {
        button.addEventListener("click", () => copyText(button.dataset.copy));
      });
      els.pairingsList.appendChild(card);
    });
    return pairings;
  } catch (error) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = error.message || "Failed to load pairings.";
    els.pairingsList.appendChild(empty);
    return [];
  }
}

async function copyPairingsSummary() {
  const device = getSelectedDevice();
  if (!device) {
    return;
  }
  const pairings = await loadPairings();
  if (!Array.isArray(pairings) || pairings.length === 0) {
    return;
  }
  const filtered = pairings
    .filter(pairing => !state.pairingsActiveOnly || pairing.status === "ACTIVE")
    .filter(pairing => !state.integrationPathFilter || state.integrationPathFilter === "ALL" || pairing.controlPath === state.integrationPathFilter);
  if (filtered.length === 0) {
    return;
  }
  const lines = filtered.map(pairing => {
    const gateway = pairing.gatewayDeviceName || pairing.gatewayDeviceId || "Direct";
    return [
      prettyLabel(pairing.controlPath),
      pairing.status,
      `Gateway: ${gateway}`,
      pairing.externalReference ? `Ref: ${pairing.externalReference}` : null,
      pairing.notes ? `Notes: ${pairing.notes}` : null
    ].filter(Boolean).join(" | ");
  });
  await copyText(lines.join("\n"));
}

async function exportPairingsJson() {
  const device = getSelectedDevice();
  if (!device) {
    return;
  }
  try {
    if (els.pairingsExportError) {
      els.pairingsExportError.classList.add("hidden");
    }
    const pairings = await loadPairings();
    const filtered = pairings
      .filter(pairing => !state.pairingsActiveOnly || pairing.status === "ACTIVE")
      .filter(pairing => !state.integrationPathFilter || state.integrationPathFilter === "ALL" || pairing.controlPath === state.integrationPathFilter);
    if (filtered.length === 0) {
      return;
    }
    const blob = new Blob([JSON.stringify(filtered, null, 2)], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `${device.id}-pairings.json`;
    anchor.click();
    URL.revokeObjectURL(url);
    els.guideError = null;
    els.adoptionSuccess = { displayName: device.displayName, deviceId: device.id };
    renderCandidateGuideBase();
    els.routeTipTitle.textContent = "Pairings exported";
    els.routeTipMessage.textContent = `Exported ${filtered.length} pairings to JSON.`;
    els.routeTipBanner.classList.remove("hidden");
  } catch (error) {
    if (els.pairingsExportError) {
      els.pairingsExportError.classList.remove("hidden");
    }
    els.guideError = { title: "Export failed", message: error.message || "Unable to export pairings." };
    renderCandidateGuideBase();
  }
}

function renderExecutions() {
  els.activityLog.innerHTML = "";
  if (state.executions.length === 0) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = "No commands yet. Tap a control to see the routing decision here.";
    els.activityLog.appendChild(empty);
    return;
  }

  state.executions.forEach(entry => {
    const fragment = els.logTemplate.content.cloneNode(true);
    fragment.querySelector(".log-command").textContent = prettyCommand(entry.command || "ERROR");
    fragment.querySelector(".log-device").textContent = entry.deviceName;
    fragment.querySelector(".log-route").textContent = prettyLabel(entry.route);
    fragment.querySelector(".log-time").textContent = new Date(entry.executedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    fragment.querySelector(".log-message").textContent = entry.message;
    if (entry.command === "PAIRING_REPAIR") {
      const item = fragment.querySelector(".log-item");
      item.classList.add("is-repair");
      if (entry.route === "ERROR") {
        item.classList.add("is-repair-error");
      }
    }
    els.activityLog.appendChild(fragment);
  });
}

async function sendCommand(command) {
  const device = getSelectedDevice();
  if (!device) {
    return;
  }

  try {
    els.routeBadge.textContent = "Routing…";
    const payload = { command };
    if (state.networkName) {
      payload.networkName = state.networkName;
    }
    const result = await api(`/api/remote/devices/${device.id}/commands`, {
      method: "POST",
      body: JSON.stringify(payload)
    });
    els.routeBadge.textContent = prettyLabel(result.route);
    state.executions = [result, ...state.executions].slice(0, 12);
    renderExecutions();
    await loadOnboardingStatus();
    renderOnboardingStatus();
  } catch (error) {
    const problem = error?.problem;
    const failureReason = problem?.reason;
    if (failureReason === "WIFI_MISMATCH") {
      els.routeBadge.textContent = "Wi-Fi mismatch";
    } else if (failureReason === "NO_VIABLE_PATH") {
      els.routeBadge.textContent = "No viable path";
    } else {
      els.routeBadge.textContent = "Route failed";
    }
    const attempted = formatAttemptedPaths(problem);
    const tip = routingTip(problem);
    const actionLabel = routeTipAction(problem);
    const reasonLabel = failureReason ? prettyLabel(failureReason) : null;
    const parts = [];
    if (reasonLabel) {
      parts.push(`Routing failed (${reasonLabel}).`);
    } else {
      parts.push(error.message);
    }
    if (attempted) {
      parts.push(`Attempted: ${attempted}.`);
    }
    if (tip) {
      parts.push(tip);
    }
    const failureMessage = parts.join(" ");
    if (tip) {
      els.routeTipTitle.textContent = "Routing tip";
      els.routeTipMessage.textContent = tip;
      els.routeTipBanner.classList.remove("hidden");
      renderRouteTipChips(problem);
      els.guideError = { title: "Routing tip", message: tip };
      renderCandidateGuideBase();
    }
    if (els.routeTipAction) {
      if (actionLabel) {
        els.routeTipAction.textContent = actionLabel;
        els.routeTipAction.classList.remove("hidden");
      } else {
        els.routeTipAction.classList.add("hidden");
      }
    }
    const failure = {
      command,
      deviceName: device.displayName,
      route: "ERROR",
      executedAt: new Date().toISOString(),
      message: failureMessage
    };
    state.executions = [failure, ...state.executions].slice(0, 12);
    renderExecutions();
  }
}

function prettyCommand(command) {
  return command.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, char => char.toUpperCase());
}

function prettyLabel(label) {
  return label.toLowerCase().replaceAll("_", " ").replace(/\b\w/g, char => char.toUpperCase());
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll("\"", "&quot;")
    .replaceAll("'", "&#039;");
}

async function copyText(value) {
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(value);
      return;
    }
  } catch {
  }
  const area = document.createElement("textarea");
  area.value = value;
  document.body.appendChild(area);
  area.select();
  document.execCommand("copy");
  area.remove();
}

function formatAttemptedPaths(problem) {
  const attempted = problem?.attemptedPaths;
  if (!Array.isArray(attempted) || attempted.length === 0) {
    return null;
  }
  return attempted.map(prettyLabel).join(" · ");
}

function renderRouteTipChips(problem) {
  if (!els.routeTipChips) {
    return;
  }
  const attempted = problem?.attemptedPaths;
  if (!Array.isArray(attempted) || attempted.length === 0) {
    els.routeTipChips.innerHTML = "";
    els.routeTipChips.classList.add("hidden");
    return;
  }
  els.routeTipChips.classList.remove("hidden");
  els.routeTipChips.innerHTML = "";
  attempted.forEach(path => {
    const chip = document.createElement("span");
    chip.className = "tip-chip";
    chip.textContent = prettyLabel(path);
    els.routeTipChips.appendChild(chip);
  });
}

function routingTip(problem) {
  const reason = problem?.reason;
  const attempted = Array.isArray(problem?.attemptedPaths) ? problem.attemptedPaths : [];
  if (reason === "WIFI_MISMATCH") {
    return "Connect to the same Wi-Fi as the TV and retry.";
  }
  if (reason === "NO_VIABLE_PATH") {
    if (attempted.includes("LAN_DIRECT") && attempted.length === 1) {
      return "Direct LAN needs pairing or the TV to be online. Try onboarding or enable Wake-on-LAN.";
    }
    if (attempted.includes("IR_GATEWAY") || attempted.includes("HDMI_CEC_GATEWAY")) {
      return "Repair pairings or bring the gateway online.";
    }
    return "Create a pairing or add a gateway path.";
  }
  return null;
}

function routeTipAction(problem) {
  const reason = problem?.reason;
  const attempted = Array.isArray(problem?.attemptedPaths) ? problem.attemptedPaths : [];
  if (reason === "NO_VIABLE_PATH" && (attempted.includes("IR_GATEWAY") || attempted.includes("HDMI_CEC_GATEWAY"))) {
    return "Repair pairings";
  }
  return null;
}

async function loadIntegrationHealth() {
  if (!els.integrationHealthList) {
    return;
  }
  try {
    els.integrationHealthList.innerHTML = "";
    const report = await api("/api/remote/health/integrations");
    if (els.integrationLastChecked) {
      els.integrationLastChecked.textContent = `Last checked: ${new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`;
    }
    if (state.integrationAutoRefresh) {
      startIntegrationCountdown();
    }
    const readyCount = report.adapters.filter(adapter => adapter.ready).length;
    const issueCount = report.adapters.length - readyCount;
    if (els.integrationReadyCount) {
      els.integrationReadyCount.textContent = `Ready ${readyCount}`;
    }
    if (els.integrationIssueCount) {
      els.integrationIssueCount.textContent = `Needs attention ${issueCount}`;
    }
    let adapters = state.integrationIssuesOnly
      ? report.adapters.filter(adapter => !adapter.ready)
      : report.adapters;
    if (state.integrationPathFilter && state.integrationPathFilter !== "ALL") {
      adapters = adapters.filter(adapter => adapter.path === state.integrationPathFilter);
    }
    if (state.integrationSortIssuesFirst) {
      adapters = adapters.slice().sort((left, right) => {
        if (left.ready === right.ready) {
          return left.adapterKey.localeCompare(right.adapterKey);
        }
        return left.ready ? 1 : -1;
      });
    }
    adapters.forEach(adapter => {
      const card = document.createElement("article");
      card.className = "integration-card";
      const detailId = `integration-detail-${adapter.adapterKey}`;
      card.innerHTML = `
        <div class="integration-card-header">
          <h3>${adapter.adapterKey}</h3>
          <div>
            <span class="badge muted">${adapter.path.replaceAll("_", " ")}</span>
            <button class="integration-toggle" type="button" data-detail="${detailId}">Details</button>
          </div>
        </div>
        <div class="integration-meta">
          <span class="integration-pill ${adapter.ready ? "ready" : "issue"}">
            <span class="integration-icon ${adapter.ready ? "ready" : "issue"}">${adapter.ready ? "✓" : "!"}</span>
            ${adapter.ready ? "Ready" : "Needs attention"}
          </span>
          <span class="integration-pill">Mode: ${adapter.desiredMode}</span>
          <span class="integration-pill">${adapter.enabled ? "Enabled" : "Disabled"}</span>
          <span class="integration-pill integration-count">Clients: ${adapter.availableClients?.length ?? 0}</span>
          ${adapter.clientAvailable ? "" : `<span class="integration-pill issue">Client missing</span>`}
        </div>
      `;
      if (adapter.missingConfig && adapter.missingConfig.length > 0) {
        const missing = document.createElement("p");
        missing.className = "muted";
        missing.textContent = `Missing: ${adapter.missingConfig.join(", ")}`;
        card.appendChild(missing);
      }
      if (adapter.issues && adapter.issues.length > 0) {
        const issues = document.createElement("p");
        issues.className = "muted";
        issues.textContent = `Issues: ${adapter.issues.join(", ")}`;
        card.appendChild(issues);
      }

      const detail = document.createElement("div");
      detail.className = "integration-detail";
      detail.id = detailId;
      const available = adapter.availableClients?.length
        ? adapter.availableClients.join(", ")
        : "None";
      const missing = adapter.missingConfig?.length
        ? adapter.missingConfig.join(", ")
        : "";
      detail.innerHTML = `
        <p class="muted">Available clients: ${available}</p>
        ${missing ? `<div class="pairing-actions"><button data-copy="${escapeHtml(missing)}" type="button">Copy missing config</button></div>` : ""}
      `;
      const copyButton = detail.querySelector("[data-copy]");
      if (copyButton) {
        copyButton.addEventListener("click", () => copyText(copyButton.dataset.copy));
      }
      const header = card.querySelector(".integration-card-header");
      if (header) {
        const copy = document.createElement("button");
        copy.className = "integration-toggle";
        copy.type = "button";
        copy.textContent = "Copy key";
        copy.addEventListener("click", () => copyText(adapter.adapterKey));
        header.appendChild(copy);
      }
      card.appendChild(detail);

      card.querySelector(".integration-toggle")?.addEventListener("click", () => {
        detail.classList.toggle("is-open");
      });

      els.integrationHealthList.appendChild(card);
    });
  } catch (error) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = error.message || "Failed to load integration health.";
    els.integrationHealthList.appendChild(empty);
    if (els.integrationReadyCount) {
      els.integrationReadyCount.textContent = "Ready 0";
    }
    if (els.integrationIssueCount) {
      els.integrationIssueCount.textContent = "Needs attention 0";
    }
    if (els.integrationLastChecked) {
      els.integrationLastChecked.textContent = "Last checked: —";
    }
  }
}

function toggleIntegrationFilter() {
  state.integrationIssuesOnly = !state.integrationIssuesOnly;
  if (els.integrationFilterButton) {
    els.integrationFilterButton.textContent = state.integrationIssuesOnly ? "Show all" : "Show issues only";
    els.integrationFilterButton.classList.toggle("is-active", state.integrationIssuesOnly);
  }
  updateIntegrationFilterState();
  loadIntegrationHealth();
}

function toggleIntegrationSort() {
  state.integrationSortIssuesFirst = !state.integrationSortIssuesFirst;
  if (els.integrationSortButton) {
    els.integrationSortButton.textContent = state.integrationSortIssuesFirst ? "Sort: Issues first" : "Sort: A–Z";
    els.integrationSortButton.classList.toggle("is-active", state.integrationSortIssuesFirst);
  }
  updateIntegrationFilterState();
  loadIntegrationHealth();
}

function toggleIntegrationAutoRefresh() {
  state.integrationAutoRefresh = !state.integrationAutoRefresh;
  if (els.integrationAutoRefreshButton) {
    els.integrationAutoRefreshButton.textContent = state.integrationAutoRefresh ? "Auto refresh: On" : "Auto refresh: Off";
    els.integrationAutoRefreshButton.classList.toggle("is-active", state.integrationAutoRefresh);
  }
  if (state.integrationAutoRefresh) {
    if (state.integrationRefreshTimer) {
      clearInterval(state.integrationRefreshTimer);
    }
    startIntegrationCountdown();
    state.integrationRefreshTimer = setInterval(loadIntegrationHealth, 30000);
  } else if (state.integrationRefreshTimer) {
    clearInterval(state.integrationRefreshTimer);
    state.integrationRefreshTimer = null;
    stopIntegrationCountdown();
  }
}

function startIntegrationCountdown() {
  if (!els.integrationCountdown) {
    return;
  }
  state.integrationCountdownRemaining = 30;
  els.integrationCountdown.classList.remove("hidden");
  updateIntegrationCountdown();
  if (state.integrationCountdownTimer) {
    clearInterval(state.integrationCountdownTimer);
  }
  state.integrationCountdownTimer = setInterval(() => {
    state.integrationCountdownRemaining = Math.max(0, state.integrationCountdownRemaining - 1);
    updateIntegrationCountdown();
  }, 1000);
}

function stopIntegrationCountdown() {
  if (state.integrationCountdownTimer) {
    clearInterval(state.integrationCountdownTimer);
    state.integrationCountdownTimer = null;
  }
  if (els.integrationCountdown) {
    els.integrationCountdown.classList.remove("hidden");
    els.integrationCountdown.textContent = "Next refresh: Paused";
  }
}

function updateIntegrationCountdown() {
  if (!els.integrationCountdown) {
    return;
  }
  els.integrationCountdown.textContent = `Next refresh: ${state.integrationCountdownRemaining}s`;
}

function bindIntegrationPathFilters() {
  const buttons = document.querySelectorAll("[data-path-filter]");
  if (!buttons.length) {
    return;
  }
  buttons.forEach(button => {
    button.addEventListener("click", () => {
      state.integrationPathFilter = button.dataset.pathFilter;
      buttons.forEach(other => other.classList.toggle("is-active", other === button));
      updateIntegrationFilterState();
      loadIntegrationHealth();
      if (els.pairingsPanel && !els.pairingsPanel.classList.contains("hidden")) {
        loadPairings();
      }
    });
  });
}

function updateIntegrationFilterState() {
  if (!els.integrationFilterState) {
    return;
  }
  const parts = [];
  if (state.integrationPathFilter && state.integrationPathFilter !== "ALL") {
    parts.push(prettyLabel(state.integrationPathFilter));
  } else {
    parts.push("All");
  }
  if (state.integrationIssuesOnly) {
    parts.push("Issues");
  }
  if (state.integrationSortIssuesFirst) {
    parts.push("Issues-first");
  } else {
    parts.push("A–Z");
  }
  els.integrationFilterState.textContent = `Filter: ${parts.join(" + ")}`;
}

function setCandidateFilter(filter) {
  state.candidateFilter = filter;
  els.candidateFilterDiscovered.classList.toggle("is-active", filter === "DISCOVERED");
  els.candidateFilterAdopted.classList.toggle("is-active", filter === "ADOPTED");
  els.candidateFilterDismissed.classList.toggle("is-active", filter === "DISMISSED");
  els.candidateFilterAll.classList.toggle("is-active", filter === "ALL");
  syncCandidateSelection();
  renderCandidates();
  refreshSelectedCandidate();
}

function validateUiBindings() {
  const required = [
    "routeTipBanner",
    "routeTipTitle",
    "routeTipMessage",
    "routeTipAction",
    "routeTipChips",
    "onboardingFailureBanner",
    "onboardingFailureTitle",
    "onboardingFailureMessage",
    "onboardingFailureAction",
    "repairPairingsButton",
    "integrationHealthList",
    "refreshIntegrationsButton",
    "integrationReadyCount",
    "integrationIssueCount",
    "integrationFilterButton",
    "integrationFilterState",
    "integrationLastChecked",
    "integrationReadyCount",
    "integrationIssueCount",
    "integrationSortButton",
    "integrationCountdown",
    "integrationAutoRefreshButton",
    "openPairingsButton",
    "pairingsPanel",
    "pairingsList",
    "refreshPairingsButton",
    "pairingsActiveOnlyButton",
    "pairingsFilterState",
    "copyPairingsButton",
    "exportPairingsButton",
    "pairingsExportError",
    "pairingsExportRetry",
    "onboardingFailureReason"
  ];
  const missing = required.filter(key => !els[key]);
  const wifiBadge = els.candidateTemplate?.content?.querySelector(".candidate-wifi");
  if (!wifiBadge) {
    missing.push("candidateTemplate.candidate-wifi");
  }
  if (missing.length > 0) {
    console.warn("UI bindings missing:", missing.join(", "));
  }
}

commandButtons.forEach(button => {
  button.addEventListener("click", () => sendCommand(button.dataset.command));
});

els.scanButton.addEventListener("click", async () => {
  try {
    await scanHome();
  } catch (error) {
    els.networkBadge.textContent = error.message;
  }
});

els.scanCandidatesButton.addEventListener("click", async () => {
  try {
    await scanCandidates();
  } catch (error) {
    els.candidateBadge.textContent = error.message;
  }
});

if (els.refreshIntegrationsButton) {
  els.refreshIntegrationsButton.addEventListener("click", loadIntegrationHealth);
}
if (els.integrationFilterButton) {
  els.integrationFilterButton.addEventListener("click", toggleIntegrationFilter);
}
if (els.integrationSortButton) {
  els.integrationSortButton.addEventListener("click", toggleIntegrationSort);
}
if (els.integrationAutoRefreshButton) {
  els.integrationAutoRefreshButton.addEventListener("click", toggleIntegrationAutoRefresh);
}

els.refreshSuggestionsButton.addEventListener("click", refreshSuggestions);
els.retryOnboardingButton.addEventListener("click", retryOnboarding);
els.openAdoptedDeviceButton.addEventListener("click", openAdoptedDevice);
els.repairPairingsButton.addEventListener("click", repairPairings);
if (els.openPairingsButton) {
  els.openPairingsButton.addEventListener("click", togglePairingsPanel);
}
if (els.refreshPairingsButton) {
  els.refreshPairingsButton.addEventListener("click", loadPairings);
}
if (els.pairingsActiveOnlyButton) {
  els.pairingsActiveOnlyButton.addEventListener("click", togglePairingsFilter);
}
if (els.copyPairingsButton) {
  els.copyPairingsButton.addEventListener("click", copyPairingsSummary);
}
if (els.exportPairingsButton) {
  els.exportPairingsButton.addEventListener("click", exportPairingsJson);
}
if (els.pairingsExportRetry) {
  els.pairingsExportRetry.addEventListener("click", exportPairingsJson);
}
if (els.routeTipAction) {
  els.routeTipAction.addEventListener("click", repairPairings);
}
if (els.onboardingFailureAction) {
  els.onboardingFailureAction.addEventListener("click", retryOnboarding);
}
els.dismissCandidateButton.addEventListener("click", () => changeCandidateStatus("dismiss"));
els.reopenCandidateButton.addEventListener("click", () => changeCandidateStatus("reopen"));
els.adoptCandidateButton.addEventListener("click", adoptSelectedCandidate);
els.candidateFilterDiscovered.addEventListener("click", () => setCandidateFilter("DISCOVERED"));
els.candidateFilterAdopted.addEventListener("click", () => setCandidateFilter("ADOPTED"));
els.candidateFilterDismissed.addEventListener("click", () => setCandidateFilter("DISMISSED"));
els.candidateFilterAll.addEventListener("click", () => setCandidateFilter("ALL"));

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => navigator.serviceWorker.register("/sw.js").catch(() => {}));
}

validateUiBindings();
bindIntegrationPathFilters();
updateIntegrationFilterState();
stopIntegrationCountdown();

Promise.all([loadDevices(), loadCandidates(), scanHome(), loadExecutions(), loadIntegrationHealth()]).catch(error => {
  els.networkBadge.textContent = error.message;
});
