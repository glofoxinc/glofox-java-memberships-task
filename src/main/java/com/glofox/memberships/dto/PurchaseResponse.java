package com.glofox.memberships.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.glofox.memberships.domain.MembershipPurchase;

import java.time.LocalDate;
import java.util.UUID;

public record PurchaseResponse(
        UUID id,

        @JsonProperty("member_name")
        String memberName,

        @JsonProperty("plan_id")
        UUID planId,

        @JsonProperty("amount_cents")
        int amountCents,

        String status,

        @JsonProperty("start_date")
        LocalDate startDate,

        @JsonProperty("transaction_id")
        String transactionId
) {

    public static PurchaseResponse from(MembershipPurchase purchase) {
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getMemberName(),
                purchase.getPlanId(),
                purchase.getAmountCents(),
                purchase.getStatus().name(),
                purchase.getStartDate(),
                purchase.getTransactionId()
        );
    }
}