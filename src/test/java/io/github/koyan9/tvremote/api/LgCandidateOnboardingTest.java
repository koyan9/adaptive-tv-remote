package io.github.koyan9.tvremote.api;

import io.github.koyan9.tvremote.integration.LgLanHandshakeClient;
import io.github.koyan9.tvremote.integration.LgLanHandshakeRequest;
import io.github.koyan9.tvremote.integration.LgLanHandshakeResult;
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

@SpringBootTest(properties = "remote.integration.adapter-modes.lg-lan=real")
@AutoConfigureMockMvc
@Transactional
class LgCandidateOnboardingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LgLanHandshakeClient lgLanHandshakeClient;

    @Test
    void adoptsLgCandidateAndPersistsOnboardingSession() throws Exception {
        given(lgLanHandshakeClient.startHandshake(any(LgLanHandshakeRequest.class)))
                .willReturn(new LgLanHandshakeResult(
                        URI.create("ws://192.168.50.31:3000"),
                        "candidate-lg-client-key",
                        "Opened an LG webOS onboarding handshake and captured a candidate client key."
                ));

        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-kitchen-lg")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-kitchen-lg/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Kitchen",
                                  "autoCreatePairings": true,
                                  "autoStartBrandOnboarding": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-kitchen-lg"));

        mockMvc.perform(get("/api/remote/devices/tv-kitchen-lg/onboarding/sessions").param("brand", "LG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].brand").value("LG"))
                .andExpect(jsonPath("$[0].negotiatedCredential").value("candidate-lg-client-key"));
    }
}
