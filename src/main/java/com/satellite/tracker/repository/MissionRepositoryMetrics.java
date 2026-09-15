package com.satellite.tracker.repository;

import com.satellite.tracker.model.Mission;
import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Thin instrumentation layer sitting directly in front of {@link MissionRepository}.
 *
 * Spring Data generates the repository implementation at runtime, so this is
 * where application-metrics for the repository/data-access layer are
 * recorded: one {@link Timer} per MongoDB operation, tracking both call
 * count and latency. The service layer depends on this class rather than on
 * {@link MissionRepository} directly.
 */
@Component
public class MissionRepositoryMetrics {

    private final MissionRepository repository;
    private final MeterRegistry meterRegistry;

    public MissionRepositoryMetrics(MissionRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    public Mission save(Mission mission) {
        return meterRegistry.timer("repository.mission.save")
                .record(() -> repository.save(mission));
    }

    public List<Mission> findAll() {
        return meterRegistry.timer("repository.mission.findAll")
                .record(() -> repository.findAll());
    }

    public Optional<Mission> findById(String id) {
        return meterRegistry.timer("repository.mission.findById")
                .record(() -> repository.findById(id));
    }

    public boolean existsById(String id) {
        return meterRegistry.timer("repository.mission.existsById")
                .record(() -> repository.existsById(id));
    }

    public void deleteById(String id) {
        meterRegistry.timer("repository.mission.deleteById")
                .record(() -> repository.deleteById(id));
    }

    public List<Mission> searchByName(String keyword) {
        return meterRegistry.timer("repository.mission.searchByName")
                .record(() -> repository.findByMissionNameContainingIgnoreCase(keyword));
    }

    public List<Mission> findByStatus(MissionStatus status) {
        return meterRegistry.timer("repository.mission.findByStatus")
                .record(() -> repository.findByStatus(status));
    }

    public List<Mission> findByOrbit(Orbit orbit) {
        return meterRegistry.timer("repository.mission.findByOrbit")
                .record(() -> repository.findByOrbit(orbit));
    }
}
