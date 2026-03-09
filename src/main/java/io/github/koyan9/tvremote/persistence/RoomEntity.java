package io.github.koyan9.tvremote.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int sortOrder;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

    protected RoomEntity() {
    }

    public RoomEntity(String id, String name, int sortOrder, HouseholdEntity household) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.household = household;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }
}
