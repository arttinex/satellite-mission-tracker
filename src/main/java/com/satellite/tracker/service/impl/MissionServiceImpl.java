package com.satellite.tracker.service.impl;

import com.satellite.tracker.dto.MissionDTO;
import com.satellite.tracker.exception.MissionNotFoundException;
import com.satellite.tracker.model.Mission;
import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import com.satellite.tracker.repository.MissionRepositoryMetrics;
import com.satellite.tracker.service.MissionService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default {@link MissionService} implementation.
 *
 * Owns mapping between {@link Mission} (persistence model) and
 * {@link MissionDTO} (API contract), input validation / business-rule
 * enforcement, and service-layer metrics. All data access goes through
 * {@link MissionRepositoryMetrics}, which itself records repository-layer
 * metrics - keeping the two concerns cleanly separated per layer.
 */
@Service
public class MissionServiceImpl implements MissionService {

    private final MissionRepositoryMetrics missionRepository;
    private final MeterRegistry meterRegistry;

    public MissionServiceImpl(MissionRepositoryMetrics missionRepository, MeterRegistry meterRegistry) {
        this.missionRepository = missionRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public MissionDTO createMission(MissionDTO missionDTO) {
        return meterRegistry.timer("service.mission.create").record(() -> {
            validate(missionDTO);
            enforceUniqueName(missionDTO.missionName(), null);

            Mission mission = toEntity(missionDTO);
            mission.setId(null); // ensure MongoDB generates a fresh id on insert
            Mission saved = missionRepository.save(mission);

            countEvent("service.mission.create.count");
            return toDto(saved);
        });
    }

    @Override
    public List<MissionDTO> getAllMissions() {
        return meterRegistry.timer("service.mission.getAll").record(() -> {
            countEvent("service.mission.getAll.count");
            return missionRepository.findAll().stream()
                    .map(this::toDto)
                    .toList();
        });
    }

    @Override
    public MissionDTO getMissionById(String id) {
        return meterRegistry.timer("service.mission.getById").record(() -> {
            countEvent("service.mission.getById.count");
            Mission mission = missionRepository.findById(id)
                    .orElseThrow(() -> new MissionNotFoundException(id));
            return toDto(mission);
        });
    }

    @Override
    public MissionDTO updateMission(String id, MissionDTO missionDTO) {
        return meterRegistry.timer("service.mission.update").record(() -> {
            validate(missionDTO);

            Mission existing = missionRepository.findById(id)
                    .orElseThrow(() -> new MissionNotFoundException(id));

            enforceUniqueName(missionDTO.missionName(), id);
            enforceStatusTransition(existing.getStatus(), missionDTO.status());

            existing.setMissionName(missionDTO.missionName().trim());
            existing.setAgency(missionDTO.agency().trim());
            existing.setLaunchDate(missionDTO.launchDate());
            existing.setOrbit(missionDTO.orbit());
            existing.setStatus(missionDTO.status());

            Mission saved = missionRepository.save(existing);
            countEvent("service.mission.update.count");
            return toDto(saved);
        });
    }

    @Override
    public void deleteMission(String id) {
        meterRegistry.timer("service.mission.delete").record(() -> {
            if (!missionRepository.existsById(id)) {
                throw new MissionNotFoundException(id);
            }
            missionRepository.deleteById(id);
            countEvent("service.mission.delete.count");
        });
    }

    @Override
    public List<MissionDTO> searchMissionsByName(String keyword) {
        return meterRegistry.timer("service.mission.search").record(() -> {
            countEvent("service.mission.search.count");
            String safeKeyword = keyword == null ? "" : keyword.trim();
            return missionRepository.searchByName(safeKeyword).stream()
                    .map(this::toDto)
                    .toList();
        });
    }

    @Override
    public List<MissionDTO> getMissionsByStatus(String status) {
        MissionStatus parsed = parseEnum(MissionStatus.class, status, "status");
        return missionRepository.findByStatus(parsed).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<MissionDTO> getMissionsByOrbit(String orbit) {
        Orbit parsed = parseEnum(Orbit.class, orbit, "orbit");
        return missionRepository.findByOrbit(parsed).stream()
                .map(this::toDto)
                .toList();
    }

    // ---------------------------------------------------------------
    // Validation & business rules
    // ---------------------------------------------------------------

    private void validate(MissionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Mission payload must not be null");
        }
        if (dto.missionName() == null || dto.missionName().isBlank()) {
            throw new IllegalArgumentException("Mission name is required");
        }
        if (dto.agency() == null || dto.agency().isBlank()) {
            throw new IllegalArgumentException("Agency is required");
        }
        if (dto.launchDate() == null) {
            throw new IllegalArgumentException("Launch date is required");
        }
        if (dto.orbit() == null) {
            throw new IllegalArgumentException("Orbit is required");
        }
        if (dto.status() == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }

    /** Business rule: mission names must be unique (case-insensitive). */
    private void enforceUniqueName(String missionName, String excludeId) {
        boolean clash = missionRepository.searchByName(missionName.trim()).stream()
                .anyMatch(m -> m.getMissionName().equalsIgnoreCase(missionName.trim())
                        && !m.getId().equals(excludeId));
        if (clash) {
            throw new IllegalArgumentException("A mission named '" + missionName + "' already exists");
        }
    }

    /** Business rule: a decommissioned mission can never be reactivated or re-planned. */
    private void enforceStatusTransition(MissionStatus current, MissionStatus requested) {
        if (current == MissionStatus.DECOMMISSIONED && requested != MissionStatus.DECOMMISSIONED) {
            throw new IllegalArgumentException("A decommissioned mission cannot change status to " + requested);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + value);
        }
    }

    private void countEvent(String name) {
        Counter.builder(name).register(meterRegistry).increment();
    }

    // ---------------------------------------------------------------
    // Mapping
    // ---------------------------------------------------------------

    private Mission toEntity(MissionDTO dto) {
        return Mission.builder()
                .id(dto.id())
                .missionName(dto.missionName().trim())
                .agency(dto.agency().trim())
                .launchDate(dto.launchDate())
                .orbit(dto.orbit())
                .status(dto.status())
                .build();
    }

    private MissionDTO toDto(Mission mission) {
        return MissionDTO.builder()
                .id(mission.getId())
                .missionName(mission.getMissionName())
                .agency(mission.getAgency())
                .launchDate(mission.getLaunchDate())
                .orbit(mission.getOrbit())
                .status(mission.getStatus())
                .build();
    }
}
