package Aerosaga.withsky.AerosagaProject.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateMissionRequest(
        @NotBlank String name,
        @NotNull @Positive Long droneId,
        @NotEmpty List<@Valid WaypointRequest> waypoints
) {
}
