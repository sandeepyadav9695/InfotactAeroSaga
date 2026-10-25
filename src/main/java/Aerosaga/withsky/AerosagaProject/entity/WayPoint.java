package Aerosaga.withsky.AerosagaProject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Entity
@Table(name = "Waypoints")
public class WayPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private double latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private double longitude;

    private Integer sequenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    @JsonIgnore
    private Mission mission;

    public WayPoint() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @DecimalMin("-90.0") @DecimalMax("90.0") double getLatitude() {
        return latitude;
    }

    public void setLatitude(@DecimalMin("-90.0") @DecimalMax("90.0") double latitude) {
        this.latitude = latitude;
    }

    public @DecimalMin("-180.0") @DecimalMax("180.0") double getLongitude() {
        return longitude;
    }

    public void setLongitude(@DecimalMin("-180.0") @DecimalMax("180.0") double longitude) {
        this.longitude = longitude;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Mission getMission() {
        return mission;
    }

    public void setMission(Mission mission) {
        this.mission = mission;
    }
}
