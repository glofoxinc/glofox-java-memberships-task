package com.glofox.memberships.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glofox.memberships.domain.Plan;

import java.util.UUID;

public record PlanResponse(
        UUID id,
        String name,
        String type,
        boolean active,

        @JsonProperty("price_cents")
        int priceCents,

        Integer credits,

        @JsonProperty("remaining_slots")
        Integer remainingSlots
) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getType().name().toLowerCase(),
                plan.isActive(),
                plan.getPriceCents(),
                plan.getCredits(),
                plan.getRemainingSlots()
        );
    }
}