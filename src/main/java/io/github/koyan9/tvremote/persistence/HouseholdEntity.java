package io.github.koyan9.tvremote.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "households")
public class HouseholdEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String networkName;

    @Column(nullable = false)
    private int sortOrder;

    protected HouseholdEntity() {
    }

    public HouseholdEntity(String id, String name, String networkName, int sortOrder) {
        this.id = id;
        this.name = name;
        this.networkName = networkName;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNetworkName() {
        return networkName;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
