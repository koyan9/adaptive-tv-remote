package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Tag;
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
@Tag("mock-integration")
class EndToEndFlowMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void scansAdoptsPairsAndSendsCommandThroughMock() throws Exception {
        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-family-tcl")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-family-tcl/adopt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomName": "Family Room",
                                  "autoCreatePairings": true,
                                  "autoStartBrandOnboarding": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("tv-family-tcl"))
                .andExpect(jsonPath("$.room").value("Family Room"));

        mockMvc.perform(get("/api/remote/devices/tv-family-tcl/pairings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].controlPath", hasItem("IR_GATEWAY")));

        mockMvc.perform(post("/api/remote/devices/tv-family-tcl/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "VOLUME_UP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("IR_GATEWAY"))
                .andExpect(jsonPath("$.brandAdapterKey").value("generic-ir"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"));
    }
}
