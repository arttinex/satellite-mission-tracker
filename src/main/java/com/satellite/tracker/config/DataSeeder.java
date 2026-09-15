package com.satellite.tracker.config;

import com.satellite.tracker.model.Mission;
import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import com.satellite.tracker.repository.MissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Seeds a few demo missions on startup if the collection is empty, so the
 * frontend has data to display immediately (mirrors the sample mockup).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final MissionRepository missionRepository;

    public DataSeeder(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    @Override
    public void run(String... args) {
        if (missionRepository.count() > 0) {
            return;
        }

        missionRepository.save(Mission.builder()
                .missionName("Hubble Telescope")
                .agency("NASA / ESA")
                .launchDate(LocalDate.of(1990, 4, 24))
                .orbit(Orbit.LEO)
                .status(MissionStatus.ACTIVE)
                .build());

        missionRepository.save(Mission.builder()
                .missionName("James Webb")
                .agency("NASA / ESA / CSA")
                .launchDate(LocalDate.of(2021, 12, 25))
                .orbit(Orbit.DEEP_SPACE)
                .status(MissionStatus.ACTIVE)
                .build());

        missionRepository.save(Mission.builder()
                .missionName("Artemis III")
                .agency("NASA")
                .launchDate(LocalDate.of(2026, 9, 1))
                .orbit(Orbit.DEEP_SPACE)
                .status(MissionStatus.PLANNED)
                .build());
    }
}
