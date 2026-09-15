package com.satellite.tracker.repository;

import com.satellite.tracker.model.Mission;
import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for {@link Mission} documents.
 *
 * Extending {@link MongoRepository} provides the standard CRUD operations
 * (save, findAll, findById, deleteById, existsById, ...). The derived query
 * methods below add the search/filter capability required by the service
 * layer without any manual query code.
 */
public interface MissionRepository extends MongoRepository<Mission, String> {

    List<Mission> findByMissionNameContainingIgnoreCase(String keyword);

    List<Mission> findByStatus(MissionStatus status);

    List<Mission> findByOrbit(Orbit orbit);
}
