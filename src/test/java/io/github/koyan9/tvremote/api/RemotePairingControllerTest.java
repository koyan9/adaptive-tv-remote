package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RemotePairingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeededPairingsForExistingDevice() throws Exception {
        mockMvc.perform(get("/api/remote/devices/tv-bedroom/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].controlPath").value("IR_GATEWAY"))
                .andExpect(jsonPath("$[0].gatewayDeviceId").value("gateway-home-hub"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void createsGatewayPairingAndUsesItForRouting() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-den",
                                  "displayName": "Den TV",
                                  "deviceType": "LEGACY_TV",
                                  "brand": "TCL",
                                  "model": "L32S5400A",
                                  "householdId": "default-home",
                                  "roomName": "Den",
                                  "online": false,
                                  "availablePaths": ["IR_GATEWAY"],
                                  "linkedGatewayIds": [],
                                  "sameWifiRequired": false,
                                  "requiresPairing": false,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["POWER_TOGGLE", "HOME", "BACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "OK"],
                                  "profileMarketingName": "Den TCL",
                                  "preferredPaths": ["IR_GATEWAY"],
                                  "profileNotes": "Registered for pairing test."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-den"));

        mockMvc.perform(post("/api/remote/pairings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-den",
                                  "controlPath": "IR_GATEWAY",
                                  "gatewayDeviceId": "gateway-home-hub",
                                  "externalReference": "manual-den-ir",
                                  "notes": "Pairing created from the first pairing API version."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("tv-den"))
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-home-hub"))
                .andExpect(jsonPath("$.controlPath").value("IR_GATEWAY"));

        mockMvc.perform(get("/api/remote/devices/tv-den/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].gatewayDeviceId", hasItem("gateway-home-hub")));

        mockMvc.perform(post("/api/remote/devices/tv-den/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("IR_GATEWAY"))
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-home-hub"));
    }
}
