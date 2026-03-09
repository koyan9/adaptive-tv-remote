package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "remote.integration.adapter-modes.samsung-lan=real")
@AutoConfigureMockMvc
class SamsungRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void switchesSamsungLanRouteToRealSkeletonClient() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-living-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.brandAdapterKey").value("samsung-lan"))
                .andExpect(jsonPath("$.protocolClientKey").value("samsung-real-lan-client"))
                .andExpect(jsonPath("$.integrationMode").value("REAL"))
                .andExpect(jsonPath("$.integrationEndpoint").value("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control"));
    }
}


