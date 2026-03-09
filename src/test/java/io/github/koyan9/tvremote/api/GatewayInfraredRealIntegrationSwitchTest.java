package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.integration.GatewayInfraredCommandRequest;
import io.github.koyan9.tvremote.integration.GatewayInfraredSessionClient;
import io.github.koyan9.tvremote.integration.GatewayInfraredSessionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "remote.integration.adapter-modes.generic-ir=real")
@AutoConfigureMockMvc
class GatewayInfraredRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayInfraredSessionClient gatewayInfraredSessionClient;

    @Test
    void switchesGatewayInfraredRouteToRealSkeletonClient() throws Exception {
        given(gatewayInfraredSessionClient.sendCommand(any(GatewayInfraredCommandRequest.class)))
                .willReturn(new GatewayInfraredSessionResult(
                        URI.create("http://gateway.local/api/infrared/send"),
                        202,
                        "Sent an infrared gateway HTTP request using profile hisense-h32a4-home."
                ));

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


