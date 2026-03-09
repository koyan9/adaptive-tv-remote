package io.github.koyan9.tvremote.service;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import io.github.koyan9.tvremote.persistence.CandidateDeviceEntity;
import io.github.koyan9.tvremote.persistence.HouseholdEntity;
import io.github.koyan9.tvremote.domain.CandidateStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Component
public class DemoDiscoveryCandidateFactory {

    public List<CandidateDeviceEntity> candidates(HouseholdEntity household) {
        return List.of(
                new CandidateDeviceEntity(
                        "candidate-loft-samsung",
                        0,
                        household,
                        "Loft Samsung TV",
                        DeviceType.SMART_TV,
                        "Samsung",
                        "QN85D",
                        "Loft",
                        true,
                        EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                        EnumSet.of(RemoteCommand.HOME, RemoteCommand.BACK, RemoteCommand.VOLUME_UP, RemoteCommand.VOLUME_DOWN, RemoteCommand.DPAD_UP, RemoteCommand.DPAD_DOWN, RemoteCommand.DPAD_LEFT, RemoteCommand.DPAD_RIGHT, RemoteCommand.OK),
                        List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                        true,
                        true,
                        true,
                        "Loft Samsung QLED",
                        "Discovered on Wi-Fi and should go through Samsung LAN onboarding.",
                        "demo-samsung-scan",
                        CandidateStatus.DISCOVERED,
                        null,
                        Instant.now()
                ),
                new CandidateDeviceEntity(
                        "candidate-kitchen-lg",
                        1,
                        household,
                        "Kitchen LG TV",
                        DeviceType.SMART_TV,
                        "LG",
                        "UT8000",
                        "Kitchen",
                        true,
                        EnumSet.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                        EnumSet.of(RemoteCommand.HOME, RemoteCommand.BACK, RemoteCommand.VOLUME_UP, RemoteCommand.VOLUME_DOWN, RemoteCommand.DPAD_UP, RemoteCommand.DPAD_DOWN, RemoteCommand.DPAD_LEFT, RemoteCommand.DPAD_RIGHT, RemoteCommand.OK),
                        List.of(ControlPath.LAN_DIRECT, ControlPath.IR_GATEWAY),
                        true,
                        true,
                        false,
                        "Kitchen webOS TV",
                        "Discovered on the home Wi-Fi and suitable for direct LAN pairing.",
                        "demo-lan-scan",
                        CandidateStatus.DISCOVERED,
                        null,
                        Instant.now()
                ),
                new CandidateDeviceEntity(
                        "candidate-playroom-projector",
                        2,
                        household,
                        "Playroom Projector",
                        DeviceType.LEGACY_TV,
                        "Epson",
                        "EH-TW7000",
                        "Playroom",
                        false,
                        EnumSet.of(ControlPath.HDMI_CEC_GATEWAY, ControlPath.IR_GATEWAY),
                        EnumSet.of(RemoteCommand.HOME, RemoteCommand.BACK, RemoteCommand.INPUT_SOURCE, RemoteCommand.DPAD_UP, RemoteCommand.DPAD_DOWN, RemoteCommand.DPAD_LEFT, RemoteCommand.DPAD_RIGHT, RemoteCommand.OK),
                        List.of(ControlPath.HDMI_CEC_GATEWAY, ControlPath.IR_GATEWAY),
                        false,
                        false,
                        false,
                        "Playroom Projector",
                        "Discovered through the gateway scan path and likely needs HDMI-CEC or infrared onboarding.",
                        "demo-gateway-scan",
                        CandidateStatus.DISCOVERED,
                        null,
                        Instant.now()
                )
        );
    }
}
