package io.github.koyan9.tvremote.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("mock-integration")
class HttpIntegrationDisabledMockTest {

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("remote.integration.default-mode", () -> "real");
        registry.add("remote.integration.adapter-modes.sony-lan", () -> "real");
        registry.add("remote.integration.adapter-modes.generic-ir", () -> "real");
        registry.add("remote.integration.adapter-modes.generic-hdmi-cec", () -> "real");
        registry.add("remote.integration.sony.enabled", () -> "false");
        registry.add("remote.integration.gateway.enabled", () -> "false");
    }

    @Test
    void returnsServiceUnavailableWhenSonyIntegrationDisabled() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-office/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.disabled"));
    }

    @Test
    void returnsServiceUnavailableWhenGatewayIntegrationDisabled() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-family-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.disabled"));
    }
}
