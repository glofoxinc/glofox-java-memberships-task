package com.glofox.memberships.controller;

import com.glofox.memberships.domain.MembershipPurchase;
import com.glofox.memberships.dto.ApiResponse;
import com.glofox.memberships.dto.PurchaseRequest;
import com.glofox.memberships.dto.PurchaseResponse;
import com.glofox.memberships.service.MembershipService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/memberships/purchases")
public class MembershipPurchaseController {

    private static final Logger log = LoggerFactory.getLogger(MembershipPurchaseController.class);

    private final MembershipService membershipService;

    public MembershipPurchaseController(
            MembershipService membershipService
    ) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseResponse>> purchaseMembership(
            @Valid @RequestBody PurchaseRequest request
    ) {
        log.info("POST /v1/memberships/purchases - memberName=\"{}\" planId={} startDate={}",
                request.memberName(), request.planId(), request.startDate());

        MembershipPurchase purchase =
                membershipService.purchaseMembership(
                        request.memberName(),
                        request.planId(),
                        request.startDate()
                );

        log.info("Purchase created successfully - purchaseId={} memberName=\"{}\" planId={}",
                purchase.getId(), purchase.getMemberName(), purchase.getPlanId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        PurchaseResponse.from(purchase)
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseResponse>> getPurchase(
            @PathVariable UUID id
    ) {
        log.info("GET /v1/memberships/purchases/{}", id);

        MembershipPurchase purchase =
                membershipService.getPurchase(id);

        log.debug("Purchase found - purchaseId={} memberName=\"{}\" status={}",
                purchase.getId(), purchase.getMemberName(), purchase.getStatus());

        return ResponseEntity.ok(
                ApiResponse.success(
                        PurchaseResponse.from(purchase)
                )
        );
    }
}