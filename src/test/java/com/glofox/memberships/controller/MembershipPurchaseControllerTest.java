package com.glofox.memberships.controller;

import com.glofox.memberships.config.PlanSeeder;
import com.glofox.memberships.payment.AlwaysSucceedingPaymentGateway;
import com.glofox.memberships.service.MembershipService;
import com.glofox.memberships.storage.memory.PlanStore;
import com.glofox.memberships.storage.memory.PurchaseStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembershipPurchaseController.class)
@Import({
        MembershipService.class,
        PlanStore.class,
        PurchaseStore.class,
        PlanSeeder.class,
        AlwaysSucceedingPaymentGateway.class
})
class MembershipPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateMembershipPurchase() throws Exception {
        String requestBody = """
                {
                  "member_name": "Alice",
                  "plan_id": "11111111-1111-1111-1111-111111111111",
                  "start_date": "2026-09-01"
                }
                """;

        mockMvc.perform(post("/v1/memberships/purchases")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.member_name").value("Alice"))
                .andExpect(jsonPath("$.data.plan_id")
                        .value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.data.amount_cents").value(8000))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.start_date").value("2026-09-01"))
                .andExpect(jsonPath("$.data.transaction_id").exists());
    }

    @Test
    void shouldGetMembershipPurchaseById() throws Exception {
        String requestBody = """
                {
                  "member_name": "Bob",
                  "plan_id": "22222222-2222-2222-2222-222222222222",
                  "start_date": "2026-09-01"
                }
                """;

        String response = mockMvc.perform(post("/v1/memberships/purchases")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String purchaseId = response
                .replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/v1/memberships/purchases/" + purchaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(purchaseId))
                .andExpect(jsonPath("$.data.member_name").value("Bob"))
                .andExpect(jsonPath("$.data.amount_cents").value(5000))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.start_date").value("2026-09-01"));
    }

    @Test
    void shouldReturnNotFoundForUnknownPurchase() throws Exception {
        mockMvc.perform(get(
                        "/v1/memberships/purchases/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void shouldRejectBlankMemberName() throws Exception {
        String requestBody = """
            {
              "member_name": "",
              "plan_id": "11111111-1111-1111-1111-111111111111",
              "start_date": "2026-09-01"
            }
            """;

        mockMvc.perform(post("/v1/memberships/purchases")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidStartDate() throws Exception {
        String requestBody = """
            {
              "member_name": "Alice",
              "plan_id": "11111111-1111-1111-1111-111111111111",
              "start_date": "2026-99-99"
            }
            """;

        mockMvc.perform(post("/v1/memberships/purchases")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}