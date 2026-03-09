package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.DeviceType;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "devices")
public class DeviceEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private boolean online;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_available_paths", joinColumns = @JoinColumn(name = "device_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "control_path", nullable = false)
    private Set<ControlPath> availablePaths = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_linked_gateways", joinColumns = @JoinColumn(name = "device_id"))
    @Column(name = "gateway_id", nullable = false)
    @OrderColumn(name = "gateway_order")
    private List<String> linkedGatewayIds = new ArrayList<>();

    @Column(nullable = false)
    private boolean sameWifiRequired;

    @Column(nullable = false)
    private boolean requiresPairing;

    @Column(nullable = false)
    private boolean supportsWakeOnLan;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_supported_commands", joinColumns = @JoinColumn(name = "device_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "remote_command", nullable = false)
    private Set<RemoteCommand> supportedCommands = new LinkedHashSet<>();

    @Column(nullable = false)
    private String profileMarketingName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "device_preferred_paths", joinColumns = @JoinColumn(name = "device_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_path", nullable = false)
    @OrderColumn(name = "path_order")
    private List<ControlPath> preferredPaths = new ArrayList<>();

    @Column(nullable = false, length = 2000)
    private String profileNotes;

    protected DeviceEntity() {
    }

    public DeviceEntity(
            String id,
            int sortOrder,
            String displayName,
            DeviceType deviceType,
            String brand,
            String model,
            boolean online,
            RoomEntity room,
            Set<ControlPath> availablePaths,
            List<String> linkedGatewayIds,
            boolean sameWifiRequired,
            boolean requiresPairing,
            boolean supportsWakeOnLan,
            Set<RemoteCommand> supportedCommands,
            String profileMarketingName,
            List<ControlPath> preferredPaths,
            String profileNotes
    ) {
        this.id = id;
        this.sortOrder = sortOrder;
        this.displayName = displayName;
        this.deviceType = deviceType;
        this.brand = brand;
        this.model = model;
        this.online = online;
        this.room = room;
        this.availablePaths = new LinkedHashSet<>(availablePaths);
        this.linkedGatewayIds = new ArrayList<>(linkedGatewayIds);
        this.sameWifiRequired = sameWifiRequired;
        this.requiresPairing = requiresPairing;
        this.supportsWakeOnLan = supportsWakeOnLan;
        this.supportedCommands = new LinkedHashSet<>(supportedCommands);
        this.profileMarketingName = profileMarketingName;
        this.preferredPaths = new ArrayList<>(preferredPaths);
        this.profileNotes = profileNotes;
    }

    public String getId() {
        return id;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public boolean isOnline() {
        return online;
    }

    public RoomEntity getRoom() {
        return room;
    }

    public Set<ControlPath> getAvailablePaths() {
        return availablePaths;
    }

    public List<String> getLinkedGatewayIds() {
        return linkedGatewayIds;
    }

    public boolean isSameWifiRequired() {
        return sameWifiRequired;
    }

    public boolean isRequiresPairing() {
        return requiresPairing;
    }

    public boolean isSupportsWakeOnLan() {
        return supportsWakeOnLan;
    }

    public Set<RemoteCommand> getSupportedCommands() {
        return supportedCommands;
    }

    public String getProfileMarketingName() {
        return profileMarketingName;
    }

    public List<ControlPath> getPreferredPaths() {
        return preferredPaths;
    }

    public String getProfileNotes() {
        return profileNotes;
    }
}
