package com.glofox.memberships.controller;

import com.glofox.memberships.dto.ApiResponse;
import com.glofox.memberships.dto.PlanResponse;
import com.glofox.memberships.storage.memory.PlanStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/plans")
public class PlanController {

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    private final PlanStore planStore;

    public PlanController(PlanStore planStore) {
        this.planStore = planStore;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PlanResponse>>> getPlans() {
        log.info("GET /v1/plans - fetching all plans");

        List<PlanResponse> plans = planStore.findAll()
                .stream()
                .map(PlanResponse::from)
                .toList();

        log.debug("GET /v1/plans - returning {} plan(s)", plans.size());

        return ResponseEntity.ok(ApiResponse.success(plans));
    }
}