package io.github.koyan9.tvremote.integration;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
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
class HttpIntegrationTimeoutMockTest {

    private static HttpServer server;
    private static int port;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        ensureServerStarted();
        registry.add("remote.integration.default-mode", () -> "real");
        registry.add("remote.integration.adapter-modes.generic-ir", () -> "real");
        registry.add("remote.integration.gateway.enabled", () -> "true");
        registry.add("remote.integration.gateway.infrared-endpoint", () -> "http://localhost:" + port + "/api/infrared/slow");
        registry.add("remote.integration.gateway.hub-id", () -> "test-hub");
        registry.add("remote.integration.gateway.auth-token", () -> "test-token");
    }

    @AfterAll
    static void shutdownServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void returnsTimeoutCodeWhenInfraredGatewayTimesOut() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-family-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME",
                                  "preferredGatewayId": "gateway-home-hub"
                                }
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.code")
                        .value("integration.transport.timeout"));
    }

    private static void ensureServerStarted() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            port = server.getAddress().getPort();
            server.createContext("/api/infrared/slow", exchange -> slowResponse(exchange));
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start mock HTTP server.", exception);
        }
    }

    private static void slowResponse(HttpExchange exchange) throws IOException {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }
}
