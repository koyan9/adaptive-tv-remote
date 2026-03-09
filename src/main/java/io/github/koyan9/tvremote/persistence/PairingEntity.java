package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.PairingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "device_pairings")
public class PairingEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "target_device_id", nullable = false)
    private DeviceEntity targetDevice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gateway_device_id")
    private DeviceEntity gatewayDevice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ControlPath controlPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PairingStatus status;

    @Column(length = 500)
    private String externalReference;

    @Column(nullable = false, length = 2000)
    private String notes;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected PairingEntity() {
    }

    public PairingEntity(
            String id,
            DeviceEntity targetDevice,
            DeviceEntity gatewayDevice,
            ControlPath controlPath,
            PairingStatus status,
            String externalReference,
            String notes
    ) {
        this.id = id;
        this.targetDevice = targetDevice;
        this.gatewayDevice = gatewayDevice;
        this.controlPath = controlPath;
        this.status = status;
        this.externalReference = externalReference;
        this.notes = notes;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public DeviceEntity getTargetDevice() {
        return targetDevice;
    }

    public DeviceEntity getGatewayDevice() {
        return gatewayDevice;
    }

    public ControlPath getControlPath() {
        return controlPath;
    }

    public PairingStatus getStatus() {
        return status;
    }

    public void setStatus(PairingStatus status) {
        this.status = status;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
