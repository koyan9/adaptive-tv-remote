package io.github.koyan9.tvremote.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.koyan9.tvremote.integration.SamsungLanCommandRequest;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeClient;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.SamsungLanHandshakeResult;
import io.github.koyan9.tvremote.integration.SamsungLanSessionClient;
import io.github.koyan9.tvremote.integration.SamsungLanSessionResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "remote.integration.adapter-modes.samsung-lan=real")
@AutoConfigureMockMvc
@Transactional
class SamsungCandidateOnboardingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SamsungLanHandshakeClient samsungLanHandshakeClient;

    @MockBean
    private SamsungLanSessionClient samsungLanSessionClient;

    @Test
    void adoptsSamsungCandidateAndPersistsHandshake() throws Exception {
        given(samsungLanHandshakeClient.startHandshake(any(SamsungLanHandshakeRequest.class)))
                .willReturn(new SamsungLanHandshakeResult(
                        URI.create("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl"),
                        "candidate-samsung-token",
                        "Opened a Samsung LAN pairing handshake and captured a candidate token."
                ));

        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-loft-samsung")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-loft-samsung/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Loft",
                                  "autoCreatePairings": true,
                                  "autoStartSamsungHandshake": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-loft-samsung"));

        mockMvc.perform(get("/api/remote/devices/tv-loft-samsung/onboarding/samsung-handshakes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].candidateId").value("candidate-loft-samsung"))
                .andExpect(jsonPath("$[0].negotiatedToken").value("candidate-samsung-token"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }

    @Test
    void usesNegotiatedSamsungHandshakeTokenForRealCommands() throws Exception {
        given(samsungLanHandshakeClient.startHandshake(any(SamsungLanHandshakeRequest.class)))
                .willReturn(new SamsungLanHandshakeResult(
                        URI.create("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl"),
                        "candidate-samsung-token",
                        "Opened a Samsung LAN pairing handshake and captured a candidate token."
                ));
        given(samsungLanSessionClient.sendCommand(any(SamsungLanCommandRequest.class)))
                .willAnswer(invocation -> {
                    SamsungLanCommandRequest request = invocation.getArgument(0);
                    return new SamsungLanSessionResult(
                            request.endpoint(),
                            "Opened a Samsung LAN WebSocket session and sent remote key " + request.remoteKey() + "."
                    );
                });

        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-loft-samsung/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Loft",
                                  "autoCreatePairings": true,
                                  "autoStartSamsungHandshake": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/devices/tv-loft-samsung/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.integrationEndpoint").value(org.hamcrest.Matchers.containsString("token=candidate-samsung-token")));
    }

    @Test
    void retriesSamsungOnboardingForAdoptedDevice() throws Exception {
        given(samsungLanHandshakeClient.startHandshake(any(SamsungLanHandshakeRequest.class)))
                .willReturn(
                        new SamsungLanHandshakeResult(
                                URI.create("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl"),
                                "candidate-samsung-token",
                                "Opened a Samsung LAN pairing handshake and captured a candidate token."
                        ),
                        new SamsungLanHandshakeResult(
                                URI.create("ws://192.168.50.21:8001/api/v2/channels/samsung.remote.control?name=QWRhcHRpdmUgVFYgUmVtb3Rl"),
                                "candidate-samsung-token-v2",
                                "Retried Samsung LAN onboarding and captured a refreshed token."
                        )
                );

        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-loft-samsung/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Loft",
                                  "autoCreatePairings": true,
                                  "autoStartBrandOnboarding": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/devices/tv-loft-samsung/onboarding/retry").param("brand", "Samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.brand").value("Samsung"))
                .andExpect(jsonPath("$.negotiatedCredential").value("candidate-samsung-token-v2"));

        mockMvc.perform(get("/api/remote/devices/tv-loft-samsung/onboarding/sessions").param("brand", "Samsung"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
