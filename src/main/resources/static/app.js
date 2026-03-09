const state = {
  devices: [],
  selectedDeviceId: null,
  executions: []
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
  capabilities: document.getElementById("capabilities")
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

async function loadDevices() {
  state.devices = await api("/api/remote/devices");
  if (!state.selectedDeviceId && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  renderSelectedDevice();
}

async function scanHome() {
  els.networkBadge.textContent = "Scanningâ€?;
  const result = await api("/api/remote/discovery/scan", { method: "POST" });
  els.networkBadge.textContent = `${result.networkName} Â· ${result.devices.length} devices`;
  state.devices = result.devices.filter(device => device.deviceType !== "GATEWAY");
  if (!state.devices.find(device => device.id === state.selectedDeviceId) && state.devices.length > 0) {
    state.selectedDeviceId = state.devices[0].id;
  }
  renderDevices();
  renderSelectedDevice();
}

async function loadExecutions() {
  state.executions = await api("/api/remote/executions");
  renderExecutions();
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

    button.addEventListener("click", () => {
      state.selectedDeviceId = device.id;
      renderDevices();
      renderSelectedDevice();
    });

    els.deviceList.appendChild(fragment);
  });
}

function renderSelectedDevice() {
  const device = getSelectedDevice();
  if (!device) {
    els.deviceName.textContent = "Select a TV";
    els.deviceMeta.textContent = "The app will choose the best available route automatically.";
    els.routeBadge.textContent = "Idle";
    els.roomValue.textContent = "â€?;
    els.brandValue.textContent = "â€?;
    els.pathsValue.textContent = "â€?;
    els.capabilities.innerHTML = "";
    commandButtons.forEach(button => button.disabled = true);
    return;
  }

  els.deviceName.textContent = device.displayName;
  els.deviceMeta.textContent = device.profile.notes;
  els.routeBadge.textContent = device.online ? "LAN preferred" : "Gateway fallback";
  els.roomValue.textContent = device.room;
  els.brandValue.textContent = `${device.brand} ${device.model}`;
  els.pathsValue.textContent = [...device.availablePaths].map(prettyLabel).join(" Â· ");

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
    els.routeBadge.textContent = "Routingâ€?;
    const result = await api(`/api/remote/devices/${device.id}/commands`, {
      method: "POST",
      body: JSON.stringify({ command })
    });
    els.routeBadge.textContent = prettyLabel(result.route);
    state.executions = [result, ...state.executions].slice(0, 12);
    renderExecutions();
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


