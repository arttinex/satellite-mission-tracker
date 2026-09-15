package com.satellite.tracker.dto;

import com.satellite.tracker.model.MissionStatus;
import com.satellite.tracker.model.Orbit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Data Transfer Object exposed by the REST API.
 *
 * Kept separate from {@link com.satellite.tracker.model.Mission} so the
 * persistence model can evolve independently of the API contract, and so
 * request-only validation rules (e.g. required fields on create) live here
 * rather than leaking persistence concerns into the API layer.
 *
 * Implemented as a record: it is an immutable data carrier with no behavior
 * of its own, which is exactly what records are for. Bean-validation
 * annotations placed on a record component are automatically propagated to
 * the generated field, constructor parameter and accessor, so validation on
 * {@code @Valid @RequestBody MissionDTO} keeps working unchanged. Lombok's
 * {@code @Builder} also supports records, so every existing
 * {@code MissionDTO.builder()...build()} call site keeps compiling as-is;
 * only direct getter calls (e.g. {@code getMissionName()}) needed to become
 * record accessor calls (e.g. {@code missionName()}).
 */
@Builder
public record MissionDTO(

        // Null on create requests; populated on responses and updates.
        String id,

        @NotBlank(message = "Mission name is required")
        String missionName,

        @NotBlank(message = "Agency is required")
        String agency,

        @NotNull(message = "Launch date is required")
        LocalDate launchDate,

        @NotNull(message = "Orbit is required")
        Orbit orbit,

        @NotNull(message = "Status is required")
        MissionStatus status
) {
}
