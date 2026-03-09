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
class RemoteManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsHouseholdsWithCounts() throws Exception {
        mockMvc.perform(get("/api/remote/households"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("default-home"))
                .andExpect(jsonPath("$[0].roomCount").value(7))
                .andExpect(jsonPath("$[0].deviceCount").value(7));
    }

    @Test
    void listsRoomsForHousehold() throws Exception {
        mockMvc.perform(get("/api/remote/rooms").param("householdId", "default-home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].householdId").value("default-home"))
                .andExpect(jsonPath("$[*].name", hasItem("Living Room")));
    }

    @Test
    void registersDeviceAndCreatesRoomWhenNeeded() throws Exception {
        mockMvc.perform(post("/api/remote/devices/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "tv-kids-room",
                                  "displayName": "Kids Room TV",
                                  "deviceType": "LEGACY_TV",
                                  "brand": "TCL",
                                  "model": "L32S5400A",
                                  "householdId": "default-home",
                                  "roomName": "Kids Room",
                                  "online": false,
                                  "availablePaths": ["IR_GATEWAY"],
                                  "linkedGatewayIds": ["gateway-home-hub"],
                                  "sameWifiRequired": false,
                                  "requiresPairing": false,
                                  "supportsWakeOnLan": false,
                                  "supportedCommands": ["POWER_TOGGLE", "HOME", "BACK", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT", "OK"],
                                  "profileMarketingName": "Kids Room TCL",
                                  "preferredPaths": ["IR_GATEWAY"],
                                  "profileNotes": "Registered from the first management API version."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-kids-room"))
                .andExpect(jsonPath("$.room").value("Kids Room"))
                .andExpect(jsonPath("$.availablePaths[0]").value("IR_GATEWAY"));

        mockMvc.perform(get("/api/remote/devices/tv-kids-room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Kids Room TV"));

        mockMvc.perform(get("/api/remote/rooms").param("householdId", "default-home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Kids Room")));
    }
}
