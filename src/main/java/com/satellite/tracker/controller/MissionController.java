package com.satellite.tracker.controller;

import com.satellite.tracker.dto.MissionDTO;
import com.satellite.tracker.service.MissionService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for satellite mission records.
 *
 * Base path: /api/missions. Thin by design - request/response mapping and
 * HTTP status selection only; all business logic lives in {@link MissionService}.
 * Each endpoint is timed via {@code @Timed} (controller-layer metric) and
 * also increments a hit counter, giving both latency and traffic-volume
 * visibility at this layer.
 */
@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private final MissionService missionService;
    private final MeterRegistry meterRegistry;

    public MissionController(MissionService missionService, MeterRegistry meterRegistry) {
        this.missionService = missionService;
        this.meterRegistry = meterRegistry;
    }

    @PostMapping
    @Timed(value = "controller.mission.create", description = "Time to create a mission")
    public ResponseEntity<MissionDTO> createMission(@Valid @RequestBody MissionDTO missionDTO) {
        hit("create");
        MissionDTO created = missionService.createMission(missionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @Timed(value = "controller.mission.getAll", description = "Time to retrieve all missions")
    public ResponseEntity<List<MissionDTO>> getAllMissions() {
        hit("getAll");
        return ResponseEntity.ok(missionService.getAllMissions());
    }

    @GetMapping("/{id}")
    @Timed(value = "controller.mission.getById", description = "Time to retrieve a mission by id")
    public ResponseEntity<MissionDTO> getMissionById(@PathVariable String id) {
        hit("getById");
        return ResponseEntity.ok(missionService.getMissionById(id));
    }

    @PutMapping("/{id}")
    @Timed(value = "controller.mission.update", description = "Time to update a mission")
    public ResponseEntity<MissionDTO> updateMission(@PathVariable String id,
                                                      @Valid @RequestBody MissionDTO missionDTO) {
        hit("update");
        return ResponseEntity.ok(missionService.updateMission(id, missionDTO));
    }

    @DeleteMapping("/{id}")
    @Timed(value = "controller.mission.delete", description = "Time to delete a mission")
    public ResponseEntity<Void> deleteMission(@PathVariable String id) {
        hit("delete");
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Timed(value = "controller.mission.search", description = "Time to search missions by name")
    public ResponseEntity<List<MissionDTO>> searchMissions(@RequestParam(required = false) String keyword) {
        hit("search");
        return ResponseEntity.ok(missionService.searchMissionsByName(keyword));
    }

    @GetMapping("/status/{status}")
    @Timed(value = "controller.mission.byStatus", description = "Time to filter missions by status")
    public ResponseEntity<List<MissionDTO>> getByStatus(@PathVariable String status) {
        hit("byStatus");
        return ResponseEntity.ok(missionService.getMissionsByStatus(status));
    }

    @GetMapping("/orbit/{orbit}")
    @Timed(value = "controller.mission.byOrbit", description = "Time to filter missions by orbit")
    public ResponseEntity<List<MissionDTO>> getByOrbit(@PathVariable String orbit) {
        hit("byOrbit");
        return ResponseEntity.ok(missionService.getMissionsByOrbit(orbit));
    }

    private void hit(String endpoint) {
        Counter.builder("controller.mission.requests")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .increment();
    }
}
