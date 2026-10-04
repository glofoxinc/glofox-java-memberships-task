package com.glofox.memberships.domain;

import java.util.UUID;

public final class Plan {

    private final UUID id;
    private final String name;
    private final PlanType type;
    private final boolean active;
    private final int priceCents;
    private final Integer credits;

    private volatile Integer remainingSlots;

    public Plan(
            UUID id,
            String name,
            PlanType type,
            boolean active,
            int priceCents,
            Integer credits,
            Integer remainingSlots) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.active = active;
        this.priceCents = priceCents;
        this.credits = credits;
        this.remainingSlots = remainingSlots;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlanType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public Integer getCredits() {
        return credits;
    }

    public synchronized boolean reserveSlot() {
        if (remainingSlots == null) {
            return true;
        }

        if (remainingSlots == 0) {
            return false;
        }

        remainingSlots--;
        return true;
    }

    public Integer getRemainingSlots() {
        return remainingSlots;
    }

    public synchronized void releaseSlot() {
        if (remainingSlots != null) {
            remainingSlots++;
        }
    }
}