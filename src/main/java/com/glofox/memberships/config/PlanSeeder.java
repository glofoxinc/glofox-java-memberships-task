package com.glofox.memberships.config;

import com.glofox.memberships.domain.Plan;
import com.glofox.memberships.domain.PlanType;
import com.glofox.memberships.storage.memory.PlanStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PlanSeeder implements CommandLineRunner {

    private final PlanStore planStore;

    public PlanSeeder(PlanStore planStore) {
        this.planStore = planStore;
    }

    @Override
    public void run(String... args) {
        for (SeedData.PlanSeed seed : SeedData.PLANS) {
            Plan plan = new Plan(
                    seed.id(),
                    seed.name(),
                    PlanType.valueOf(seed.type().toUpperCase()),
                    seed.active(),
                    seed.priceCents(),
                    seed.credits(),
                    seed.remainingSlots()
            );

            planStore.save(plan);
        }
    }
}