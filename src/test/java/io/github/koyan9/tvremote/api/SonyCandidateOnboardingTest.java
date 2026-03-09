package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.integration.SonyLanHandshakeClient;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.SonyLanHandshakeResult;
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

@SpringBootTest(properties = "remote.integration.adapter-modes.sony-lan=real")
@AutoConfigureMockMvc
@Transactional
class SonyCandidateOnboardingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SonyLanHandshakeClient sonyLanHandshakeClient;

    @Test
    void adoptsSonyCandidateAndPersistsOnboardingSession() throws Exception {
        given(sonyLanHandshakeClient.startHandshake(any(SonyLanHandshakeRequest.class)))
                .willReturn(new SonyLanHandshakeResult(
                        URI.create("http://192.168.50.41/sony/accessControl"),
                        "candidate-sony-psk",
                        "Opened a Sony BRAVIA onboarding handshake and captured a candidate pre-shared key."
                ));

        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-study-sony")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-study-sony/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Study",
                                  "autoCreatePairings": true,
                                  "autoStartBrandOnboarding": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-study-sony"));

        mockMvc.perform(get("/api/remote/devices/tv-study-sony/onboarding/sessions").param("brand", "Sony"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("Sony"))
                .andExpect(jsonPath("$[0].negotiatedCredential").value("candidate-sony-psk"));
    }
}
