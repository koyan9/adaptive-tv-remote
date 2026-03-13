package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RemotePairingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    void revokesPairingAndBlocksImplicitGatewayFallbackForPairedPath() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-playroom",
                                  "displayName": "Playroom TV",
                                  "deviceType": "LEGACY_TV",
                                  "brand": "Skyworth",
                                  "model": "32E2A",
                                  "householdId": "default-home",
                                  "roomName": "Playroom",
                                  "online": false,
                                  "availablePaths": ["IR_GATEWAY"],
                                  "linkedGatewayIds": ["gateway-home-hub"],
                                  "sameWifiRequired": false,
                                  "requiresPairing": false,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["POWER_TOGGLE", "HOME", "BACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "OK"],
                                  "profileMarketingName": "Playroom Skyworth",
                                  "preferredPaths": ["IR_GATEWAY"],
                                  "profileNotes": "Registered for revoke test."
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult pairingResult = mockMvc.perform(post("/api/remote/pairings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-playroom",
                                  "controlPath": "IR_GATEWAY",
                                  "gatewayDeviceId": "gateway-home-hub",
                                  "externalReference": "playroom-ir",
                                  "notes": "Initial active pairing."
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode pairingJson = objectMapper.readTree(pairingResult.getResponse().getContentAsString());
        String pairingId = pairingJson.get("id").asText();

        mockMvc.perform(delete("/api/remote/pairings/{pairingId}", pairingId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/remote/devices/tv-playroom/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REVOKED"));

        mockMvc.perform(post("/api/remote/devices/tv-playroom/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("No viable control path is available for device Playroom TV"));
    }

    @Test
    void activatesLatestPairingAndRevokesOlderPairingForSamePath() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "gateway-bedroom-hub",
                                  "displayName": "Bedroom Hub",
                                  "deviceType": "GATEWAY",
                                  "brand": "Koyan",
                                  "model": "Home Hub Mini",
                                  "householdId": "default-home",
                                  "roomName": "Bedroom",
                                  "online": true,
                                  "availablePaths": ["IR_GATEWAY"],
                                  "linkedGatewayIds": [],
                                  "sameWifiRequired": false,
                                  "requiresPairing": false,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["HOME", "BACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "OK"],
                                  "profileMarketingName": "Bedroom Hub",
                                  "preferredPaths": ["IR_GATEWAY"],
                                  "profileNotes": "Registered as alternate gateway."
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult firstPairing = mockMvc.perform(post("/api/remote/pairings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-guest-room",
                                  "controlPath": "IR_GATEWAY",
                                  "gatewayDeviceId": "gateway-home-hub",
                                  "externalReference": "guest-original",
                                  "notes": "Original active pairing."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult seededPairings = mockMvc.perform(get("/api/remote/devices/tv-guest-room/pairings"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode seededArray = objectMapper.readTree(seededPairings.getResponse().getContentAsString());
        String seededPairingId = seededArray.get(0).get("id").asText();

        mockMvc.perform(post("/api/remote/pairings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-guest-room",
                                  "controlPath": "IR_GATEWAY",
                                  "gatewayDeviceId": "gateway-bedroom-hub",
                                  "externalReference": "guest-rebind",
                                  "notes": "Rebound to bedroom hub."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-bedroom-hub"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/remote/devices/tv-guest-room/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].gatewayDeviceId", hasItem("gateway-bedroom-hub")));

        mockMvc.perform(patch("/api/remote/pairings/{pairingId}", seededPairingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ACTIVE",
                                  "notes": "Reactivated original pairing."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-home-hub"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/remote/devices/tv-guest-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-home-hub"));
    }

    @Test
    void repairsMissingPairingsForDevice() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-repair-room",
                                  "displayName": "Repair Room TV",
                                  "deviceType": "SMART_TV",
                                  "brand": "TestBrand",
                                  "model": "R-100",
                                  "householdId": "default-home",
                                  "roomName": "Repair Room",
                                  "online": false,
                                  "availablePaths": ["LAN_DIRECT", "IR_GATEWAY"],
                                  "linkedGatewayIds": ["gateway-home-hub"],
                                  "sameWifiRequired": false,
                                  "requiresPairing": true,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["POWER_TOGGLE", "HOME", "BACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "OK"],
                                  "profileMarketingName": "Repair TV",
                                  "preferredPaths": ["LAN_DIRECT", "IR_GATEWAY"],
                                  "profileNotes": "Registered for repair test."
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/devices/tv-repair-room/pairings/repair"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].controlPath", hasItem("LAN_DIRECT")))
                .andExpect(jsonPath("$[*].controlPath", hasItem("IR_GATEWAY")));

        mockMvc.perform(post("/api/remote/devices/tv-repair-room/commands")
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

    @Test
    void returnsRoutingFailureDetailsWithAttemptedPaths() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-nopath",
                                  "displayName": "No Path TV",
                                  "deviceType": "LEGACY_TV",
                                  "brand": "TestBrand",
                                  "model": "NP-1",
                                  "householdId": "default-home",
                                  "roomName": "Lab",
                                  "online": false,
                                  "availablePaths": ["IR_GATEWAY"],
                                  "linkedGatewayIds": [],
                                  "sameWifiRequired": false,
                                  "requiresPairing": true,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["HOME"],
                                  "profileMarketingName": "No Path",
                                  "preferredPaths": ["IR_GATEWAY"],
                                  "profileNotes": "Requires pairing but none exists."
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/devices/tv-nopath/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("NO_VIABLE_PATH"))
                .andExpect(jsonPath("$.attemptedPaths[0]").value("IR_GATEWAY"));
    }
}
