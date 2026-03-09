package io.github.koyan9.tvremote.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RemoteControlControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsTelevisionDevices() throws Exception {
        mockMvc.perform(get("/api/remote/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("tv-living-room"))
                .andExpect(jsonPath("$[0].availablePaths[0]").exists());
    }

    @Test
    void routesBedroomTelevisionThroughInfraredGateway() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-bedroom/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "VOLUME_UP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("IR_GATEWAY"))
                .andExpect(jsonPath("$.gatewayDeviceId").value("gateway-home-hub"))
                .andExpect(jsonPath("$.brandAdapterKey").value("generic-ir"))
                .andExpect(jsonPath("$.protocolFamily").value("Infrared Code Library (simulated)"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"))
                .andExpect(jsonPath("$.integrationEndpoint").value("simulated://generic-ir"));
    }

    @Test
    void usesMockHdmiCecClientByDefaultForCinemaDisplay() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-cinema-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("HDMI_CEC_GATEWAY"))
                .andExpect(jsonPath("$.brandAdapterKey").value("generic-hdmi-cec"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"))
                .andExpect(jsonPath("$.integrationEndpoint").value("simulated://generic-hdmi-cec"));
    }

    @Test
    void usesMockSamsungClientByDefaultForLanRoute() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-living-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.brandAdapterKey").value("samsung-lan"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"));
    }

    @Test
    void usesMockLgClientByDefaultForLanRoute() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-studio/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.brandAdapterKey").value("lg-lan"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"));
    }

    @Test
    void usesMockSonyClientByDefaultForLanRoute() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-office/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "HOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.route").value("LAN_DIRECT"))
                .andExpect(jsonPath("$.brandAdapterKey").value("sony-lan"))
                .andExpect(jsonPath("$.protocolClientKey").value("mock-protocol-client"))
                .andExpect(jsonPath("$.integrationMode").value("MOCK"));
    }

    @Test
    void rejectsUnsupportedLegacyCommand() throws Exception {
        mockMvc.perform(post("/api/remote/devices/tv-guest-room/commands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "command": "CHANNEL_UP"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Command CHANNEL_UP is not supported for device Guest Room Legacy TV"));
    }

    @Test
    void exposesStandaloneProjectProfileAndAdapters() throws Exception {
        mockMvc.perform(get("/api/remote/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Adaptive TV Remote"))
                .andExpect(jsonPath("$.standalone").value(true))
                .andExpect(jsonPath("$.brandAdapters[0].adapterKey").exists());
    }

    @Test
    void listsRegisteredAdapters() throws Exception {
        mockMvc.perform(get("/api/remote/adapters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].adapterKey").exists())
                .andExpect(jsonPath("$[0].protocolFamily").exists());
    }

    @Test
    void exposesIntegrationConfiguration() throws Exception {
        mockMvc.perform(get("/api/remote/integrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defaultMode").value("MOCK"))
                .andExpect(jsonPath("$.registeredClients[0].clientKey").exists())
                .andExpect(jsonPath("$.samsungEndpoint").exists())
                .andExpect(jsonPath("$.sonyEndpoint").exists())
                .andExpect(jsonPath("$.lgEndpoint").exists())
                .andExpect(jsonPath("$.gatewayInfraredEndpoint").exists())
                .andExpect(jsonPath("$.gatewayHdmiCecEndpoint").exists());
    }
}


