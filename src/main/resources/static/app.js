const state = {
  devices: [],
  candidates: [],
  selectedDeviceId: null,
  selectedCandidateId: null,
  candidateSuggestions: [],
  executions: [],
  onboardingStatus: null
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
  onboardingDetail: document.getElementById("onboardingDetail"),
  candidateState: document.getElementById("candidateState"),
  candidateName: document.getElementById("candidateName"),
  candidateMeta: document.getElementById("candidateMeta"),
  candidateSummary: document.getElementById("candidateSummary"),
  candidateSuggestions: document.getElementById("candidateSuggestions"),
  adoptCandidateButton: document.getElementById("adoptCandidateButton")
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
    throw new Error(problem.detail || "Request failed.");
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
  if (!state.selectedCandidateId && state.candidates.length > 0) {
    state.selectedCandidateId = state.candidates[0].id;
  }
  renderCandidates();
  await refreshSelectedCandidate();
}

async function scanHome() {
  els.networkBadge.textContent = "Scanning…";
  const result = await api("/api/remote/discovery/scan", { method: "POST" });
  els.networkBadge.textContent = `${result.networkName} · ${result.devices.length} devices`;
  state.devices = result.devices.filter(device => device.deviceType !== "GATEWAY");
  if (!state.devices.find(device => device.id === state.selectedDeviceId) && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  await refreshSelectedDevice();
}

async function scanCandidates() {
  els.candidateBadge.textContent = "Scanning…";
  state.candidates = await api("/api/remote/discovery/candidates/scan", { method: "POST" });
  els.candidateBadge.textContent = `${state.candidates.length} candidates`;
  if (!state.candidates.find(candidate => candidate.id === state.selectedCandidateId) && state.candidates.length > 0) {
    state.selectedCandidateId = state.candidates[0].id;
  }
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
  if (!candidate) {
    state.candidateSuggestions = [];
    return;
  }

  try {
    state.candidateSuggestions = await api(`/api/remote/discovery/candidates/${candidate.id}/pairing-suggestions`);
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
  if (state.candidates.length === 0) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = "No candidate devices yet. Use Find candidates to scan the home.";
    els.candidateList.appendChild(empty);
    els.candidateBadge.textContent = "No candidates";
    return;
  }

  els.candidateBadge.textContent = `${state.candidates.length} candidates`;
  state.candidates.forEach(candidate => {
    const fragment = els.candidateTemplate.content.cloneNode(true);
    const button = fragment.querySelector("button");
    button.classList.toggle("is-selected", candidate.id === state.selectedCandidateId);
    fragment.querySelector(".candidate-room").textContent = candidate.roomName;
    fragment.querySelector(".candidate-name").textContent = candidate.displayName;
    fragment.querySelector(".candidate-model").textContent = `${candidate.brand} ${candidate.model}`;

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
}

function renderOnboardingStatus() {
  const device = getSelectedDevice();
  const status = state.onboardingStatus;

  if (!device) {
    els.onboardingState.textContent = "No session";
    els.onboardingProvider.textContent = "—";
    els.onboardingSessionCount.textContent = "0";
    els.onboardingCredential.textContent = "—";
    els.onboardingDetail.textContent = "Select a device to view onboarding status.";
    return;
  }

  if (!status || !status.onboardingSupported) {
    els.onboardingState.textContent = "Not required";
    els.onboardingProvider.textContent = "—";
    els.onboardingSessionCount.textContent = "0";
    els.onboardingCredential.textContent = "—";
    els.onboardingDetail.textContent = "This device does not require brand onboarding for its current integration path.";
    return;
  }

  els.onboardingState.textContent = status.latestStatus || "No session";
  els.onboardingProvider.textContent = status.latestProviderId || status.brand;
  els.onboardingSessionCount.textContent = String(status.sessionCount ?? 0);
  els.onboardingCredential.textContent = status.negotiatedCredentialPresent ? (status.credentialPreview || "Stored") : "Not negotiated";
  els.onboardingDetail.textContent = status.latestDetail || "No onboarding session has been recorded yet.";
}

function renderCandidateGuideBase() {
  const candidate = getSelectedCandidate();
  if (!candidate) {
    els.candidateState.textContent = "No candidate";
    els.candidateName.textContent = "Select a candidate";
    els.candidateMeta.textContent = "Scan the home to discover candidate TVs and projectors that can be adopted into the device catalog.";
    els.candidateSummary.textContent = "No candidate selected.";
    els.candidateSuggestions.innerHTML = "";
    els.adoptCandidateButton.disabled = true;
    return;
  }

  els.candidateState.textContent = candidate.status;
  els.candidateName.textContent = candidate.displayName;
  els.candidateMeta.textContent = `${candidate.brand} ${candidate.model} · ${candidate.roomName} · ${candidate.discoverySource}`;
  els.candidateSummary.textContent = candidate.online
    ? "This candidate is online and ready for direct onboarding or adaptive fallback."
    : "This candidate is offline and may need gateway-assisted onboarding.";
  els.adoptCandidateButton.disabled = candidate.status !== "DISCOVERED";
}

function renderCandidateGuide() {
  const candidate = getSelectedCandidate();
  els.candidateSuggestions.innerHTML = "";

  if (!candidate) {
    return;
  }

  if (state.candidateSuggestions.length === 0) {
    const empty = document.createElement("p");
    empty.className = "muted";
    empty.textContent = "No pairing suggestions available for this candidate yet.";
    els.candidateSuggestions.appendChild(empty);
    return;
  }

  state.candidateSuggestions.forEach(suggestion => {
    const card = document.createElement("article");
    card.className = "suggestion-card";
    card.innerHTML = `
      <div class="suggestion-top">
        <strong>${prettyLabel(suggestion.controlPath)}</strong>
        <span class="badge muted">${suggestion.autoSelectable ? "Auto" : "Manual"}</span>
      </div>
      <p class="muted">${suggestion.rationale}</p>
      <div class="suggestion-meta">${suggestion.gatewayDeviceName || "Direct LAN onboarding"}</div>
    `;
    els.candidateSuggestions.appendChild(card);
  });
}

async function adoptSelectedCandidate() {
  const candidate = getSelectedCandidate();
  if (!candidate || candidate.status !== "DISCOVERED") {
    return;
  }

  const payload = {
    roomName: candidate.roomName,
    autoCreatePairings: true,
    autoStartBrandOnboarding: true
  };

  try {
    els.adoptCandidateButton.disabled = true;
    els.candidateState.textContent = "Adopting…";
    const adopted = await api(`/api/remote/discovery/candidates/${candidate.id}/adopt`, {
      method: "POST",
      body: JSON.stringify(payload)
    });

    await Promise.all([loadDevices(), loadCandidates(), loadExecutions()]);
    state.selectedDeviceId = adopted.id;
    renderDevices();
    await refreshSelectedDevice();
    await refreshSelectedCandidate();
  } catch (error) {
    els.candidateSummary.textContent = error.message;
    els.adoptCandidateButton.disabled = false;
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
    const result = await api(`/api/remote/devices/${device.id}/commands`, {
      method: "POST",
      body: JSON.stringify({ command })
    });
    els.routeBadge.textContent = prettyLabel(result.route);
    state.executions = [result, ...state.executions].slice(0, 12);
    renderExecutions();
    await loadOnboardingStatus();
    renderOnboardingStatus();
  } catch (error) {
    els.routeBadge.textContent = "Route failed";
    const failure = {
      command,
      deviceName: device.displayName,
      route: "ERROR",
      executedAt: new Date().toISOString(),
      message: error.message
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

els.adoptCandidateButton.addEventListener("click", adoptSelectedCandidate);

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => navigator.serviceWorker.register("/sw.js").catch(() => {}));
}

Promise.all([loadDevices(), loadCandidates(), scanHome(), loadExecutions()]).catch(error => {
  els.networkBadge.textContent = error.message;
});
