package Aerosaga.withsky.AerosagaProject.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name="Drones")
public class Drone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Min(0)
    @Max(100)
    @Column(nullable = false)
    private double batteryLevel = 100.0;

    @Column(nullable = false)
    private String status = "IDLE";

    @Column(nullable = false)
    private String gpsStatus = "CONNECTED";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @Min(0) @Max(100) double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(@Min(0) @Max(100) double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGpsStatus() {
        return gpsStatus;
    }

    public void setGpsStatus(String gpsStatus) {
        this.gpsStatus = gpsStatus;
    }
}
