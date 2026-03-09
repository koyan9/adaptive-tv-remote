package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.LgHandshakeStatus;
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
@Table(name = "lg_handshakes")
public class LgHandshakeEntity {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private DeviceEntity device;

    @Column(nullable = false)
    private String candidateId;

    @Column(nullable = false)
    private String endpoint;

    @Column(nullable = false)
    private String clientIdentity;

    @Column
    private String negotiatedClientKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LgHandshakeStatus status;

    @Column(nullable = false, length = 2000)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected LgHandshakeEntity() {
    }

    public LgHandshakeEntity(
            String id,
            DeviceEntity device,
            String candidateId,
            String endpoint,
            String clientIdentity,
            String negotiatedClientKey,
            LgHandshakeStatus status,
            String detail
    ) {
        this.id = id;
        this.device = device;
        this.candidateId = candidateId;
        this.endpoint = endpoint;
        this.clientIdentity = clientIdentity;
        this.negotiatedClientKey = negotiatedClientKey;
        this.status = status;
        this.detail = detail;
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

    public String getId() { return id; }
    public DeviceEntity getDevice() { return device; }
    public String getCandidateId() { return candidateId; }
    public String getEndpoint() { return endpoint; }
    public String getClientIdentity() { return clientIdentity; }
    public String getNegotiatedClientKey() { return negotiatedClientKey; }
    public LgHandshakeStatus getStatus() { return status; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
