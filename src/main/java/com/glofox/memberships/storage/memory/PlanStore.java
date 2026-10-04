package com.glofox.memberships.storage.memory;

import com.glofox.memberships.domain.Plan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlanStore {

    private static final Logger log = LoggerFactory.getLogger(PlanStore.class);

    private final Map<UUID, Plan> plans = new ConcurrentHashMap<>();

    /**
     * Persists a new plan into the in-memory store, keyed by its ID.
     */
    public void save(Plan plan) {
        log.debug("Saving plan - planId={} name=\"{}\" type={}", plan.getId(), plan.getName(), plan.getType());
        plans.put(plan.getId(), plan);
        log.debug("Plan saved - planId={}", plan.getId());
    }

    /**
     * Returns all plans currently held in the store as a new list.
     */
    public List<Plan> findAll() {
        log.debug("Fetching all plans - count={}", plans.size());
        return new ArrayList<>(plans.values());
    }

    /**
     * Looks up a plan by its ID. Returns null if no plan exists with the given ID.
     */
    public Plan findById(UUID planId) {
        Plan plan = plans.get(planId);
        if (plan == null) {
            log.debug("Plan lookup miss - planId={}", planId);
        } else {
            log.debug("Plan lookup hit - planId={} name=\"{}\"", planId, plan.getName());
        }
        return plan;
    }

    /**
     * Attempts to reserve one slot on a promo plan by decrementing its remaining slot count.
     * Returns true if the slot was successfully reserved, false if the plan is sold out or not found.
     */
    public boolean reserveSlot(UUID planId) {
        Plan plan = plans.get(planId);

        if (plan == null) {
            log.warn("Attempted slot reservation on unknown plan - planId={}", planId);
            return false;
        }

        boolean reserved = plan.reserveSlot();
        if (reserved) {
            log.debug("Promo slot reserved - planId={} remainingSlots={}", planId, plan.getRemainingSlots());
        } else {
            log.warn("Promo slot reservation failed (sold out) - planId={}", planId);
        }
        return reserved;
    }

    /**
     * Releases a previously reserved promo slot, incrementing the remaining slot count back.
     * Called during purchase rollback if payment or a later step fails.
     */
    public void releaseSlot(UUID planId) {
        Plan plan = plans.get(planId);

        if (plan != null) {
            plan.releaseSlot();
            log.debug("Promo slot released - planId={} remainingSlots={}", planId, plan.getRemainingSlots());
        } else {
            log.warn("Attempted slot release on unknown plan - planId={}", planId);
        }
    }
}