package io.github.koyan9.tvremote.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("mock-integration")
class HttpIntegrationMockTest {

    private static final AtomicReference<CapturedRequest> IRCC_REQUEST = new AtomicReference<>();
    private static final AtomicReference<CapturedRequest> IR_REQUEST = new AtomicReference<>();
    private static final AtomicReference<CapturedRequest> CEC_REQUEST = new AtomicReference<>();
    private static HttpServer server;
    private static int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    void resetRequests() {
        IRCC_REQUEST.set(null);
        IR_REQUEST.set(null);
        CEC_REQUEST.set(null);
    }

    @Test
    void sendsSonyIrccCommandWithPskHeader() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-office/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk());

        CapturedRequest request = IRCC_REQUEST.get();
        assertThat(request).isNotNull();
        assertThat(request.path()).isEqualTo("/sony/ircc");
        assertThat(request.header("X-Auth-PSK")).isEqualTo("test-psk");
        assertThat(request.header("SOAPACTION")).isEqualTo("\"urn:schemas-sony-com:service:IRCC:1#X_SendIRCC\"");
        assertThat(request.body()).contains("<IRCCCode>AAAAAQAAAAEAAABgAw==</IRCCCode>");
    }

    @Test
    void sendsInfraredGatewayPayloadWithIrCode() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-family-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk());

        CapturedRequest request = IR_REQUEST.get();
        assertThat(request).isNotNull();
        assertThat(request.path()).isEqualTo("/api/infrared/send");
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token");
        assertThat(request.header("X-Hub-Id")).isEqualTo("test-hub");

        JsonNode payload = objectMapper.readTree(request.body());
        assertThat(payload.get("transport").asText()).isEqualTo("infrared");
        assertThat(payload.get("command").get("profileKey").asText()).isEqualTo("tcl-q7-home");
        assertThat(payload.get("command").get("actionKey").asText()).isEqualTo("home");
        assertThat(payload.get("command").get("format").asText()).isEqualTo("tasmota-irsend");
        assertThat(payload.get("command").get("ir").get("protocol").asText()).isEqualTo("NEC");
        assertThat(payload.get("command").get("ir").get("bits").asInt()).isEqualTo(32);
        assertThat(payload.get("command").get("ir").get("data").asText()).isEqualTo("0x00FF48B7");
        assertThat(payload.get("command").get("ir").get("repeat").asInt()).isEqualTo(1);
    }

    @Test
    void sendsInfraredGatewayPayloadWithProfileKeyWhenCodeMissing() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-family-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "VOLUME_UP"
                                }
                                """))
                .andExpect(status().isOk());

        CapturedRequest request = IR_REQUEST.get();
        assertThat(request).isNotNull();
        JsonNode payload = objectMapper.readTree(request.body());
        assertThat(payload.get("command").get("profileKey").asText()).isEqualTo("tcl-q7-volume-up");
        assertThat(payload.get("command").get("actionKey").asText()).isEqualTo("volume-up");
        assertThat(payload.get("command").get("format").asText()).isEqualTo("profile-key");
        assertThat(payload.get("command").get("ir")).isNull();
    }

    @Test
    void sendsHdmiCecGatewayPayloadWithHex() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-cinema-room/commands")
                        .contentType("application/json")
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk());

        CapturedRequest request = CEC_REQUEST.get();
        assertThat(request).isNotNull();
        assertThat(request.path()).isEqualTo("/api/cec/send");
        assertThat(request.header("Authorization")).isEqualTo("Bearer test-token");
        assertThat(request.header("X-Hub-Id")).isEqualTo("test-hub");

        JsonNode payload = objectMapper.readTree(request.body());
        assertThat(payload.get("transport").asText()).isEqualTo("hdmi-cec");
        assertThat(payload.get("command").get("actionKey").asText()).isEqualTo("cec-home");
        assertThat(payload.get("command").get("format").asText()).isEqualTo("cec-hex");
        assertThat(payload.get("command").get("cec").get("payloadHex").asText()).isEqualTo("40:44");
    }

    private static void ensureServerStarted() {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(0), 0);
            port = server.getAddress().getPort();
            server.createContext("/sony/ircc", exchange -> handle(exchange, IRCC_REQUEST));
            server.createContext("/api/infrared/send", exchange -> handle(exchange, IR_REQUEST));
            server.createContext("/api/cec/send", exchange -> handle(exchange, CEC_REQUEST));
            server.start();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start mock HTTP server.", exception);
        }
    }

    private static void handle(HttpExchange exchange, AtomicReference<CapturedRequest> target) throws IOException {
        CapturedRequest captured = capture(exchange);
        target.set(captured);
        byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private static CapturedRequest capture(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        exchange.getRequestHeaders().forEach((key, value) -> headers.put(key, List.copyOf(value)));
        return new CapturedRequest(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), headers, body);
    }

    private record CapturedRequest(
            String method,
            String path,
            Map<String, List<String>> headers,
            String body
    ) {
        String header(String name) {
            List<String> values = headers.get(name);
            return values == null || values.isEmpty() ? null : values.get(0);
        }
    }
}
