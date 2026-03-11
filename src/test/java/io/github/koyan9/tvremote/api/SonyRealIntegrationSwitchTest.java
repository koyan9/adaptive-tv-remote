package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import io.github.koyan9.tvremote.integration.SonyIrccCommandRequest;
import io.github.koyan9.tvremote.integration.SonyLanSessionClient;
import io.github.koyan9.tvremote.integration.SonyLanSessionResult;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = "remote.integration.adapter-modes.sony-lan=real")
@AutoConfigureMockMvc
class SonyRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SonyLanSessionClient sonyLanSessionClient;

    @Test
    void switchesSonyLanRouteToRealSkeletonClient() throws Exception {
        given(sonyLanSessionClient.sendCommand(any(SonyIrccCommandRequest.class)))
                .willReturn(new SonyLanSessionResult(
                        URI.create("http://192.168.50.41/sony/ircc"),
                        200,
                        "Sent Sony IRCC command HOME using code AAAAAQAAAAEAAABgAw==."
                ));

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
                .andExpect(jsonPath("$.integrationEndpoint").value("http://192.168.50.41/sony/ircc"));
    }
}


