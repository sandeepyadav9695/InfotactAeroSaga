package Aerosaga.withsky.AerosagaProject.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "missions")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status = "CREATED";

    @Column(name = "workflow_id")
    private String workflowId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "drone_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Drone drone;

    @OneToMany(
            mappedBy = "mission",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<WayPoint> waypoints = new ArrayList<>();

    public Mission() {
    }

    public void addWaypoint(WayPoint waypoint) {
        waypoints.add(waypoint);
        waypoint.setMission(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Drone getDrone() {
        return drone;
    }

    public void setDrone(Drone drone) {
        this.drone = drone;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return workflowId;
    }

    public List<WayPoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<WayPoint> waypoints) {
        this.waypoints = waypoints;
    }
}
