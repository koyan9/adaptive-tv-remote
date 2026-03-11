package io.github.koyan9.tvremote.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("mock-integration")
class HttpIntegrationFailureMockTest {

    private static HttpServer server;
    private static int port;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        ensureServerStarted();
        registry.add("remote.integration.default-mode", () -> "real");
        registry.add("remote.integration.adapter-modes.sony-lan", () -> "real");
        registry.add("remote.integration.adapter-modes.generic-ir", () -> "real");
        registry.add("remote.integration.adapter-modes.generic-hdmi-cec", () -> "real");
        registry.add("remote.integration.sony.enabled", () -> "true");
        registry.add("remote.integration.sony.endpoint", () -> "http://localhost:" + port + "/sony/accessControl");
        registry.add("remote.integration.sony.pre-shared-key", () -> "test-psk");
        registry.add("remote.integration.sony.ircc-endpoint", () -> "http://localhost:" + port + "/sony/ircc");
        registry.add("remote.integration.gateway.enabled", () -> "true");
        registry.add("remote.integration.gateway.infrared-endpoint", () -> "http://localhost:" + port + "/api/infrared/send");
        registry.add("remote.integration.gateway.hdmi-cec-endpoint", () -> "http://localhost:" + port + "/api/cec/send");
        registry.add("remote.integration.gateway.hub-id", () -> "test-hub");
        registry.add("remote.integration.gateway.auth-token", () -> "test-token");
        registry.add("remote.integration.gateway.ir-codes.tcl-q7-home.protocol", () -> "NEC");
        registry.add("remote.integration.gateway.ir-codes.tcl-q7-home.bits", () -> "32");
        registry.add("remote.integration.gateway.ir-codes.tcl-q7-home.data", () -> "0x00FF48B7");
        registry.add("remote.integration.gateway.ir-codes.tcl-q7-home.repeat", () -> "1");
        registry.add("remote.integration.gateway.cec-commands.cec-home", () -> "40:44");
    }

    @AfterAll
    static void shutdownServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @BeforeEach
    void resetServerState() {
        // No-op; endpoints are fixed to return failures in this test.
    }

    @Test
    void returnsBadGatewayWhenSonyIrccFails() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-office/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.transport.failure"));
    }

    @Test
    void returnsBadGatewayWhenInfraredGatewayFails() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-family-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.transport.failure"));
    }

    @Test
    void returnsBadGatewayWhenHdmiCecGatewayFails() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-cinema-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.transport.failure"));
    }

    private static void ensureServerStarted() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            port = server.getAddress().getPort();
            server.createContext("/sony/ircc", exchange -> respond(exchange, 403));
            server.createContext("/api/infrared/send", exchange -> respond(exchange, 500));
            server.createContext("/api/cec/send", exchange -> respond(exchange, 401));
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start mock HTTP server.", exception);
        }
    }

    private static void respond(HttpExchange exchange, int status) throws IOException {
        byte[] response = ("error-" + status).getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

}
