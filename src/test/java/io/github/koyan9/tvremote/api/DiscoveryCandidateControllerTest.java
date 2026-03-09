package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiscoveryCandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void scansAndListsDiscoveredCandidates() throws Exception {
        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("DISCOVERED"));

        mockMvc.perform(get("/api/remote/discovery/candidates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-kitchen-lg")))
                .andExpect(jsonPath("$[*].id", hasItem("candidate-playroom-projector")));
    }

    @Test
    void returnsPairingSuggestionsForGatewayCandidate() throws Exception {
        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/remote/discovery/candidates/candidate-playroom-projector/pairing-suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].controlPath", hasItem("HDMI_CEC_GATEWAY")))
                .andExpect(jsonPath("$[*].gatewayDeviceId", hasItem("gateway-home-hub")));
    }

    @Test
    void adoptsCandidateAndCreatesSuggestedPairings() throws Exception {
        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-playroom-projector/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Playroom",
                                  "autoCreatePairings": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-playroom-projector"))
                .andExpect(jsonPath("$.room").value("Playroom"));

        mockMvc.perform(get("/api/remote/devices/tv-playroom-projector/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].controlPath", hasItem("HDMI_CEC_GATEWAY")));

        mockMvc.perform(get("/api/remote/discovery/candidates").param("status", "ADOPTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].adoptedDeviceId", hasItem("tv-playroom-projector")));
    }
}
