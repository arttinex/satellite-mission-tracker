package com.satellite.tracker.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

/**
 * MongoDB document representing a single satellite mission.
 *
 * Mapped to the "missions" collection. Field-level mapping annotations
 * ({@link Field}) control the physical document key names, and Bean
 * Validation annotations enforce data integrity before persistence.
 */
@Document(collection = "missions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mission {

    @Id
    private String id;

    @NotBlank(message = "Mission name is required")
    @Field("mission_name")
    @Indexed
    private String missionName;

    @NotBlank(message = "Agency is required")
    @Field("agency")
    private String agency;

    @NotNull(message = "Launch date is required")
    @Field("launch_date")
    private LocalDate launchDate;

    @NotNull(message = "Orbit type is required")
    @Field("orbit")
    private Orbit orbit;

    @NotNull(message = "Status is required")
    @Field("status")
    @Indexed
    private MissionStatus status;
}
