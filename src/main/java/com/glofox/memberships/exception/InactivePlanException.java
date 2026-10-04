package com.glofox.memberships.exception;

import java.util.UUID;

public class InactivePlanException extends RuntimeException {

    public InactivePlanException(UUID planId) {
        super("Plan is inactive: " + planId);
    }
}