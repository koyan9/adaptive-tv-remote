package io.github.koyan9.tvremote.persistence;

import io.github.koyan9.tvremote.domain.ControlPath;
import io.github.koyan9.tvremote.domain.RemoteCommand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "command_executions")
public class CommandExecutionEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RemoteCommand command;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ControlPath route;

    @Column
    private String gatewayDeviceId;

    @Column(nullable = false)
    private String adapterLabel;

    @Column(nullable = false)
    private String brandAdapterKey;

    @Column(nullable = false)
    private String protocolFamily;

    @Column(nullable = false)
    private String protocolClientKey;

    @Column(nullable = false)
    private String integrationMode;

    @Column(nullable = false)
    private String integrationEndpoint;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false)
    private Instant executedAt;

    protected CommandExecutionEntity() {
    }

    public CommandExecutionEntity(
            String id,
            String deviceId,
            String deviceName,
            RemoteCommand command,
            ControlPath route,
            String gatewayDeviceId,
            String adapterLabel,
            String brandAdapterKey,
            String protocolFamily,
            String protocolClientKey,
            String integrationMode,
            String integrationEndpoint,
            String status,
            String message,
            Instant executedAt
    ) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.command = command;
        this.route = route;
        this.gatewayDeviceId = gatewayDeviceId;
        this.adapterLabel = adapterLabel;
        this.brandAdapterKey = brandAdapterKey;
        this.protocolFamily = protocolFamily;
        this.protocolClientKey = protocolClientKey;
        this.integrationMode = integrationMode;
        this.integrationEndpoint = integrationEndpoint;
        this.status = status;
        this.message = message;
        this.executedAt = executedAt;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public RemoteCommand getCommand() {
        return command;
    }

    public ControlPath getRoute() {
        return route;
    }

    public String getGatewayDeviceId() {
        return gatewayDeviceId;
    }

    public String getAdapterLabel() {
        return adapterLabel;
    }

    public String getBrandAdapterKey() {
        return brandAdapterKey;
    }

    public String getProtocolFamily() {
        return protocolFamily;
    }

    public String getProtocolClientKey() {
        return protocolClientKey;
    }

    public String getIntegrationMode() {
        return integrationMode;
    }

    public String getIntegrationEndpoint() {
        return integrationEndpoint;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }
}
