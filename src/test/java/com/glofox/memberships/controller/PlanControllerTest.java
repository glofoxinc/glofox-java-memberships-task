package com.glofox.memberships.controller;

import com.glofox.memberships.config.PlanSeeder;
import com.glofox.memberships.storage.memory.PlanStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PlanController.class)
@Import({PlanStore.class, PlanSeeder.class})
class PlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnAllPlansIncludingInactivePlan() throws Exception {
        mockMvc.perform(get("/v1/plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(4))

                // Monthly Unlimited
                .andExpect(jsonPath(
                        "$.data[?(@.name == 'Monthly Unlimited')]"
                ).exists())

                // 10-Class Pack
                .andExpect(jsonPath(
                        "$.data[?(@.name == '10-Class Pack')]"
                ).exists())

                // Launch Promo Pack
                .andExpect(jsonPath(
                        "$.data[?(@.name == 'Launch Promo Pack')]"
                ).exists())

                // Legacy Plan must be present and inactive
                .andExpect(jsonPath(
                        "$.data[?(@.name == 'Legacy Plan')].active"
                ).value(false))

                // Promo must start with 5 slots
                .andExpect(jsonPath(
                        "$.data[?(@.name == 'Launch Promo Pack')].remaining_slots"
                ).value(5));
    }
}