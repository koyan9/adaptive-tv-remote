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

@SpringBootTest(properties = "remote.integration.adapter-modes.sony-lan=real")
@AutoConfigureMockMvc
class SonyRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void switchesSonyLanRouteToRealSkeletonClient() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-office/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.brandAdapterKey").value("sony-lan"))
                .andExpect(jsonPath("$.protocolClientKey").value("sony-real-lan-client"))
                .andExpect(jsonPath("$.integrationMode").value("REAL"))
                .andExpect(jsonPath("$.integrationEndpoint").value("http://192.168.50.41/sony/accessControl"));
    }
}


