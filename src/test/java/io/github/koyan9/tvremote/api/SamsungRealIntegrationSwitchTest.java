package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.integration.SamsungLanCommandRequest;
import io.github.koyan9.tvremote.integration.SamsungLanSessionClient;
import io.github.koyan9.tvremote.integration.SamsungLanSessionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(properties = "remote.integration.adapter-modes.samsung-lan=real")
@AutoConfigureMockMvc
class SamsungRealIntegrationSwitchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SamsungLanSessionClient samsungLanSessionClient;

    @Test
    void switchesSamsungLanRouteToRealSkeletonClient() throws Exception {
        given(samsungLanSessionClient.sendCommand(any(SamsungLanCommandRequest.class)))
                .willReturn(new SamsungLanSessionResult(
                        java.net.URI.create("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl&token=demo-samsung-token"),
                        "Opened a Samsung LAN WebSocket session and sent remote key KEY_HOME."
                ));

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
                .andExpect(jsonPath("$.integrationEndpoint").value("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl&token=demo-samsung-token"));
    }
}


