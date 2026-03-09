const state = {
  devices: [],
  selectedDeviceId: null,
  executions: [],
  onboardingStatus: null
};

const els = {
  deviceList: document.getElementById("deviceList"),
  deviceTemplate: document.getElementById("deviceCardTemplate"),
  logTemplate: document.getElementById("logItemTemplate"),
  activityLog: document.getElementById("activityLog"),
  scanButton: document.getElementById("scanButton"),
  networkBadge: document.getElementById("networkBadge"),
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
  onboardingDetail: document.getElementById("onboardingDetail")
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

  return response.json();
}

async function refreshSelectedDevice() {
  renderSelectedDeviceBase();
  await loadOnboardingStatus();
  renderOnboardingStatus();
}

async function loadDevices() {
  state.devices = await api("/api/remote/devices");
  if (!state.selectedDeviceId && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  await refreshSelectedDevice();
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

function getSelectedDevice() {
  return state.devices.find(device => device.id === state.selectedDeviceId) || null;
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

if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => navigator.serviceWorker.register("/sw.js").catch(() => {}));
}

Promise.all([loadDevices(), scanHome(), loadExecutions()]).catch(error => {
  els.networkBadge.textContent = error.message;
});
