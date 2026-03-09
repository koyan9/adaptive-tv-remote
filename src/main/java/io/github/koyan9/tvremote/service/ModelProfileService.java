package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.model.ModelProfile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelProfileService {

    public ModelProfile samsungLivingRoomProfile() {
        return new ModelProfile(
                "Samsung",
                "QN90",
                "Neo QLED Living Room",
                List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                "Prefers local Wi-Fi control and falls back to the home gateway when direct pairing is unavailable."
        );
    }

    public ModelProfile sonyBedroomProfile() {
        return new ModelProfile(
                "Sony",
                "XR-55A80",
                "Bedroom Bravia OLED",
                List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                "Direct control is supported, but this device is currently simulated as offline to exercise IR fallback."
        );
    }

    public ModelProfile sonyOfficeProfile() {
        return new ModelProfile(
                "Sony",
                "XR-65X90L",
                "Office Bravia XR",
                List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                "Prefers direct BRAVIA LAN control and stays online for real skeleton protocol validation."
        );
    }

    public ModelProfile lgStudioProfile() {
        return new ModelProfile(
                "LG",
                "OLED55C3",
                "Studio webOS OLED",
                List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                "Prefers direct webOS LAN control and can fall back to the home gateway when needed."
        );
    }

    public ModelProfile legacyGuestRoomProfile() {
        return new ModelProfile(
                "Hisense",
                "H32A4",
                "Legacy Guest Room TV",
                List.of(ControlPath.IR_GATEWAY),
                "No network API. Requires a gateway with an IR code profile."
        );
    }

    public ModelProfile cinemaRoomProfile() {
        return new ModelProfile(
                "BenQ",
                "W4000i",
                "Cinema Room Display",
                List.of(ControlPath.HDMI_CEC_GATEWAY, ControlPath.IR_GATEWAY),
                "Routes through the home gateway first because the display is controlled primarily over HDMI-CEC."
        );
    }

    public ModelProfile gatewayProfile() {
        return new ModelProfile(
                "Koyan",
                "Home Hub One",
                "Universal Control Gateway",
                List.of(ControlPath.IR_GATEWAY, ControlPath.HDMI_CEC_GATEWAY),
                "Always-on box bridging the phone app to IR and HDMI-CEC control paths."
        );
    }
}


