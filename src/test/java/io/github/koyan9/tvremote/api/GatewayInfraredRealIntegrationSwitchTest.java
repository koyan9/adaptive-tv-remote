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

@SpringBootTest(properties = "remote.integration.adapter-modes.generic-ir=real")
@AutoConfigureMockMvc
class GatewayInfraredRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void switchesGatewayInfraredRouteToRealSkeletonClient() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-guest-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("IR_GATEWAY"))
                .andExpect(jsonPath("$.brandAdapterKey").value("generic-ir"))
                .andExpect(jsonPath("$.protocolClientKey").value("gateway-ir-real-client"))
                .andExpect(jsonPath("$.integrationMode").value("REAL"))
                .andExpect(jsonPath("$.integrationEndpoint").value("http://gateway.local/api/infrared/send"));
    }
}


