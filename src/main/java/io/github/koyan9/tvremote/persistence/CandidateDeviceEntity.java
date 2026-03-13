package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.CandidateStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "candidate_devices")
public class CandidateDeviceEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

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
    private String roomName;

    @Column(nullable = false)
    private boolean online;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_available_paths", joinColumns = @JoinColumn(name = "candidate_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "control_path", nullable = false)
    private Set<ControlPath> availablePaths = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_supported_commands", joinColumns = @JoinColumn(name = "candidate_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "remote_command", nullable = false)
    private Set<RemoteCommand> supportedCommands = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_preferred_paths", joinColumns = @JoinColumn(name = "candidate_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_path", nullable = false)
    @OrderColumn(name = "path_order")
    private List<ControlPath> preferredPaths = new ArrayList<>();

    @Column(nullable = false)
    private boolean sameWifiRequired;

    @Column(nullable = false)
    private boolean requiresPairing;

    @Column(nullable = false)
    private boolean supportsWakeOnLan;

    @Column(nullable = false)
    private String profileMarketingName;

    @Column(nullable = false, length = 2000)
    private String profileNotes;

    @Column(nullable = false)
    private String discoverySource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateStatus status;

    @Column
    private String adoptedDeviceId;

    @Column(nullable = false)
    private Instant lastSeenAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected CandidateDeviceEntity() {
    }

    public CandidateDeviceEntity(
            String id,
            int sortOrder,
            HouseholdEntity household,
            String displayName,
            DeviceType deviceType,
            String brand,
            String model,
            String roomName,
            boolean online,
            Set<ControlPath> availablePaths,
            Set<RemoteCommand> supportedCommands,
            List<ControlPath> preferredPaths,
            boolean sameWifiRequired,
            boolean requiresPairing,
            boolean supportsWakeOnLan,
            String profileMarketingName,
            String profileNotes,
            String discoverySource,
            CandidateStatus status,
            String adoptedDeviceId,
            Instant lastSeenAt
    ) {
        this.id = id;
        this.sortOrder = sortOrder;
        this.household = household;
        this.displayName = displayName;
        this.deviceType = deviceType;
        this.brand = brand;
        this.model = model;
        this.roomName = roomName;
        this.online = online;
        this.availablePaths = new LinkedHashSet<>(availablePaths);
        this.supportedCommands = new LinkedHashSet<>(supportedCommands);
        this.preferredPaths = new ArrayList<>(preferredPaths);
        this.sameWifiRequired = sameWifiRequired;
        this.requiresPairing = requiresPairing;
        this.supportsWakeOnLan = supportsWakeOnLan;
        this.profileMarketingName = profileMarketingName;
        this.profileNotes = profileNotes;
        this.discoverySource = discoverySource;
        this.status = status;
        this.adoptedDeviceId = adoptedDeviceId;
        this.lastSeenAt = lastSeenAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public int getSortOrder() { return sortOrder; }
    public HouseholdEntity getHousehold() { return household; }
    public String getDisplayName() { return displayName; }
    public DeviceType getDeviceType() { return deviceType; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getRoomName() { return roomName; }
    public boolean isOnline() { return online; }
    public Set<ControlPath> getAvailablePaths() { return availablePaths; }
    public Set<RemoteCommand> getSupportedCommands() { return supportedCommands; }
    public List<ControlPath> getPreferredPaths() { return preferredPaths; }
    public boolean isSameWifiRequired() { return sameWifiRequired; }
    public boolean isRequiresPairing() { return requiresPairing; }
    public boolean isSupportsWakeOnLan() { return supportsWakeOnLan; }
    public String getProfileMarketingName() { return profileMarketingName; }
    public String getProfileNotes() { return profileNotes; }
    public String getDiscoverySource() { return discoverySource; }
    public CandidateStatus getStatus() { return status; }
    public String getAdoptedDeviceId() { return adoptedDeviceId; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setOnline(boolean online) { this.online = online; }

    public void markSeen() {
        this.lastSeenAt = Instant.now();
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public void setAdoptedDeviceId(String adoptedDeviceId) {
        this.adoptedDeviceId = adoptedDeviceId;
    }

    public boolean refreshFromScan(CandidateDeviceEntity scanned) {
        boolean changed = false;
        if (!Objects.equals(displayName, scanned.displayName)) {
            displayName = scanned.displayName;
            changed = true;
        }
        if (deviceType != scanned.deviceType) {
            deviceType = scanned.deviceType;
            changed = true;
        }
        if (!Objects.equals(brand, scanned.brand)) {
            brand = scanned.brand;
            changed = true;
        }
        if (!Objects.equals(model, scanned.model)) {
            model = scanned.model;
            changed = true;
        }
        if (!Objects.equals(roomName, scanned.roomName)) {
            roomName = scanned.roomName;
            changed = true;
        }
        if (online != scanned.online) {
            online = scanned.online;
            changed = true;
        }
        if (!Objects.equals(availablePaths, scanned.availablePaths)) {
            availablePaths = new LinkedHashSet<>(scanned.availablePaths);
            changed = true;
        }
        if (!Objects.equals(supportedCommands, scanned.supportedCommands)) {
            supportedCommands = new LinkedHashSet<>(scanned.supportedCommands);
            changed = true;
        }
        if (!Objects.equals(preferredPaths, scanned.preferredPaths)) {
            preferredPaths = new ArrayList<>(scanned.preferredPaths);
            changed = true;
        }
        if (sameWifiRequired != scanned.sameWifiRequired) {
            sameWifiRequired = scanned.sameWifiRequired;
            changed = true;
        }
        if (requiresPairing != scanned.requiresPairing) {
            requiresPairing = scanned.requiresPairing;
            changed = true;
        }
        if (supportsWakeOnLan != scanned.supportsWakeOnLan) {
            supportsWakeOnLan = scanned.supportsWakeOnLan;
            changed = true;
        }
        if (!Objects.equals(profileMarketingName, scanned.profileMarketingName)) {
            profileMarketingName = scanned.profileMarketingName;
            changed = true;
        }
        if (!Objects.equals(profileNotes, scanned.profileNotes)) {
            profileNotes = scanned.profileNotes;
            changed = true;
        }
        if (!Objects.equals(discoverySource, scanned.discoverySource)) {
            discoverySource = scanned.discoverySource;
            changed = true;
        }
        return changed;
    }
}
