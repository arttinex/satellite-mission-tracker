package com.satellite.tracker.exception;

/**
 * Thrown when a mission lookup by id does not match any stored document.
 */
public class MissionNotFoundException extends RuntimeException {

    public MissionNotFoundException(String id) {
        super("Mission not found with id: " + id);
    }
}
