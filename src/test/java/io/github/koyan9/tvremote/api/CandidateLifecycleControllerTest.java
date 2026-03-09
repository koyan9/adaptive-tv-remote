package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class CandidateLifecycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void dismissesAndReopensCandidate() throws Exception {
        mockMvc.perform(post("/api/remote/discovery/candidates/scan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-playroom-projector")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-playroom-projector/dismiss"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"));

        mockMvc.perform(get("/api/remote/discovery/candidates").param("status", "DISMISSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-playroom-projector")));

        mockMvc.perform(post("/api/remote/discovery/candidates/candidate-playroom-projector/reopen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISCOVERED"));

        mockMvc.perform(get("/api/remote/discovery/candidates").param("status", "DISCOVERED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("candidate-playroom-projector")));
    }
}
