package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.integration.GatewayHdmiCecCommandRequest;
import io.github.koyan9.tvremote.integration.GatewayHdmiCecSessionClient;
import io.github.koyan9.tvremote.integration.GatewayHdmiCecSessionResult;
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

@SpringBootTest(properties = "remote.integration.adapter-modes.generic-hdmi-cec=real")
@AutoConfigureMockMvc
class GatewayHdmiCecRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GatewayHdmiCecSessionClient gatewayHdmiCecSessionClient;

    @Test
    void switchesGatewayHdmiCecRouteToRealSkeletonClient() throws Exception {
        given(gatewayHdmiCecSessionClient.sendCommand(any(GatewayHdmiCecCommandRequest.class)))
                .willReturn(new GatewayHdmiCecSessionResult(
                        URI.create("http://gateway.local/api/cec/send"),
                        202,
                        "Sent an HDMI-CEC gateway HTTP request using action cec-home."
                ));

        mockMvc.perform(post("/api/remote/devices/tv-cinema-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("HDMI_CEC_GATEWAY"))
                .andExpect(jsonPath("$.brandAdapterKey").value("generic-hdmi-cec"))
                .andExpect(jsonPath("$.protocolClientKey").value("gateway-hdmi-cec-real-client"))
                .andExpect(jsonPath("$.integrationMode").value("REAL"))
                .andExpect(jsonPath("$.integrationEndpoint").value("http://gateway.local/api/cec/send"));
    }
}


