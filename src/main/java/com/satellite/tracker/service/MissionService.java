package com.satellite.tracker.service;

import com.satellite.tracker.dto.MissionDTO;

import java.util.List;

/**
 * Business-layer contract for satellite mission operations.
 *
 * Declared as an interface (Dependency Inversion) so the REST controller
 * depends on this abstraction rather than on {@link com.satellite.tracker.service.impl.MissionServiceImpl}
 * directly, keeping the layers loosely coupled and independently testable.
 */
public interface MissionService {

    /**
     * Validates and persists a new mission.
     */
    MissionDTO createMission(MissionDTO missionDTO);

    /**
     * Retrieves every stored mission.
     */
    List<MissionDTO> getAllMissions();

    /**
     * Retrieves a single mission by its id.
     *
     * @throws com.satellite.tracker.exception.MissionNotFoundException if no mission matches
     */
    MissionDTO getMissionById(String id);

    /**
     * Updates an existing mission's fields.
     *
     * @throws com.satellite.tracker.exception.MissionNotFoundException if no mission matches
     */
    MissionDTO updateMission(String id, MissionDTO missionDTO);

    /**
     * Deletes a mission by id.
     *
     * @throws com.satellite.tracker.exception.MissionNotFoundException if no mission matches
     */
    void deleteMission(String id);

    /**
     * Searches missions whose name contains the given keyword (case-insensitive).
     */
    List<MissionDTO> searchMissionsByName(String keyword);

    /**
     * Filters missions by status.
     */
    List<MissionDTO> getMissionsByStatus(String status);

    /**
     * Filters missions by orbit type.
     */
    List<MissionDTO> getMissionsByOrbit(String orbit);
}
