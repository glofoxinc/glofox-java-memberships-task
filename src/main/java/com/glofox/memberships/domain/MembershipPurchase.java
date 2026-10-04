package com.glofox.memberships.domain;

import java.time.LocalDate;
import java.util.UUID;

public final class MembershipPurchase {

    private final UUID id;
    private final String memberName;
    private final UUID planId;
    private final int amountCents;
    private final PurchaseStatus status;
    private final LocalDate startDate;
    private final String transactionId;

    public MembershipPurchase(
            UUID id,
            String memberName,
            UUID planId,
            int amountCents,
            PurchaseStatus status,
            LocalDate startDate,
            String transactionId) {

        this.id = id;
        this.memberName = memberName;
        this.planId = planId;
        this.amountCents = amountCents;
        this.status = status;
        this.startDate = startDate;
        this.transactionId = transactionId;
    }

    public UUID getId() {
        return id;
    }

    public String getMemberName() {
        return memberName;
    }

    public UUID getPlanId() {
        return planId;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public PurchaseStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getTransactionId() {
        return transactionId;
    }
}