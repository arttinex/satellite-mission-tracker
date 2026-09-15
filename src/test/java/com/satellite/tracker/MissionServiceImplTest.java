package com.satellite.tracker;

import com.satellite.tracker.dto.MissionDTO;
import com.satellite.tracker.exception.MissionNotFoundException;
import com.satellite.tracker.model.Mission;
import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import com.satellite.tracker.repository.MissionRepositoryMetrics;
import com.satellite.tracker.service.impl.MissionServiceImpl;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MissionServiceImplTest {

    private MissionRepositoryMetrics repository;
    private MissionServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(MissionRepositoryMetrics.class);
        service = new MissionServiceImpl(repository, new SimpleMeterRegistry());
    }

    @Test
    void createMission_savesAndReturnsDto() {
        MissionDTO dto = MissionDTO.builder()
                .missionName("Voyager 2")
                .agency("NASA")
                .launchDate(LocalDate.of(1977, 8, 20))
                .orbit(Orbit.DEEP_SPACE)
                .status(MissionStatus.ACTIVE)
                .build();

        when(repository.searchByName(anyString())).thenReturn(List.of());
        when(repository.save(any(Mission.class))).thenAnswer(inv -> {
            Mission m = inv.getArgument(0);
            m.setId("generated-id");
            return m;
        });

        MissionDTO result = service.createMission(dto);

        assertThat(result.id()).isEqualTo("generated-id");
        assertThat(result.missionName()).isEqualTo("Voyager 2");
        verify(repository, times(1)).save(any(Mission.class));
    }

    @Test
    void getMissionById_throwsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMissionById("missing"))
                .isInstanceOf(MissionNotFoundException.class);
    }

    @Test
    void updateMission_rejectsReactivatingDecommissionedMission() {
        Mission existing = Mission.builder()
                .id("m1")
                .missionName("Old Sat")
                .agency("NASA")
                .launchDate(LocalDate.of(2000, 1, 1))
                .orbit(Orbit.LEO)
                .status(MissionStatus.DECOMMISSIONED)
                .build();

        when(repository.findById("m1")).thenReturn(Optional.of(existing));
        when(repository.searchByName(anyString())).thenReturn(List.of());

        MissionDTO update = MissionDTO.builder()
                .missionName("Old Sat")
                .agency("NASA")
                .launchDate(LocalDate.of(2000, 1, 1))
                .orbit(Orbit.LEO)
                .status(MissionStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> service.updateMission("m1", update))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
