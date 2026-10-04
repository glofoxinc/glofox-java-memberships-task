package com.glofox.memberships.service;

import com.glofox.memberships.domain.MembershipPurchase;
import com.glofox.memberships.domain.Plan;
import com.glofox.memberships.domain.PlanType;
import com.glofox.memberships.domain.PurchaseStatus;
import com.glofox.memberships.exception.DuplicateUnlimitedMembershipException;
import com.glofox.memberships.exception.InactivePlanException;
import com.glofox.memberships.exception.PlanNotFoundException;
import com.glofox.memberships.exception.PromoSoldOutException;
import com.glofox.memberships.payment.PaymentGateway;
import com.glofox.memberships.storage.memory.PlanStore;
import com.glofox.memberships.storage.memory.PurchaseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class MembershipService {

    private static final Logger log = LoggerFactory.getLogger(MembershipService.class);

    private final PlanStore planStore;
    private final PurchaseStore purchaseStore;
    private final PaymentGateway paymentGateway;

    public MembershipService(
            PlanStore planStore,
            PurchaseStore purchaseStore,
            PaymentGateway paymentGateway
    ) {
        this.planStore = planStore;
        this.purchaseStore = purchaseStore;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Fetches the plan by ID and checks it is active before allowing a purchase.
     * Throws {@link PlanNotFoundException} if the ID doesn't match any plan.
     * Throws {@link InactivePlanException} if the plan exists but has been deactivated.
     */
    public Plan getValidPlan(UUID planId) {
        log.debug("Validating plan - planId={}", planId);

        Plan plan = planStore.findById(planId);

        if (plan == null) {
            log.warn("Plan not found - planId={}", planId);
            throw new PlanNotFoundException(planId);
        }

        if (!plan.isActive()) {
            log.warn("Plan is inactive - planId={} name=\"{}\"", planId, plan.getName());
            throw new InactivePlanException(planId);
        }

        log.debug("Plan validated - planId={} name=\"{}\" type={}", planId, plan.getName(), plan.getType());
        return plan;
    }

    /**
     * Handles the full purchase flow: validates the plan, reserves slots if needed,
     * charges payment, and saves the purchase.
     * For UNLIMITED plans, prevents a member from holding more than one active membership.
     * For PROMO plans, decrements the available slot count atomically to avoid overselling.
     * If payment or any step fails after a reservation is made, the reservation is rolled back.
     */
    public MembershipPurchase purchaseMembership(
            String memberName,
            UUID planId,
            LocalDate startDate
    ) {
        log.info("Purchasing membership - memberName=\"{}\" planId={} startDate={}", memberName, planId, startDate);

        Plan plan = getValidPlan(planId);

        UUID purchaseId = UUID.randomUUID();

        boolean unlimitedReserved = false;
        boolean promoSlotReserved = false;

        try {
            /*
             * For unlimited plans, only one active membership
             * is allowed per member.
             */
            if (plan.getType() == PlanType.UNLIMITED) {
                log.debug("Reserving unlimited membership slot - memberName=\"{}\" purchaseId={}", memberName, purchaseId);
                unlimitedReserved =
                        purchaseStore.reserveUnlimitedMembership(
                                memberName,
                                purchaseId
                        );

                if (!unlimitedReserved) {
                    log.warn("Duplicate unlimited membership rejected - memberName=\"{}\"", memberName);
                    throw new DuplicateUnlimitedMembershipException(
                            memberName
                    );
                }
                log.debug("Unlimited membership slot reserved - memberName=\"{}\"", memberName);
            }

            /*
             * Promo plans have a limited number of slots.
             * Plan.reserveSlot() is synchronized, so concurrent
             * requests cannot oversell the available slots.
             */
            if (plan.getRemainingSlots() != null) {
                log.debug("Reserving promo slot - planId={} remainingSlots={}", planId, plan.getRemainingSlots());
                promoSlotReserved = planStore.reserveSlot(planId);

                if (!promoSlotReserved) {
                    log.warn("Promo plan sold out - planId={} name=\"{}\"", planId, plan.getName());
                    throw new PromoSoldOutException();
                }
                log.debug("Promo slot reserved - planId={}", planId);
            }

            /*
             * Payment is performed only after the required
             * membership/slot reservation succeeds.
             */
            log.info("Charging payment - memberName=\"{}\" priceCents={}", memberName, plan.getPriceCents());
            PaymentGateway.PaymentResult paymentResult =
                    paymentGateway.charge(
                            plan.getPriceCents(),
                            memberName
                    );
            log.info("Payment successful - memberName=\"{}\" transactionId={}", memberName, paymentResult.transactionId());

            MembershipPurchase purchase =
                    new MembershipPurchase(
                            purchaseId,
                            memberName,
                            plan.getId(),
                            plan.getPriceCents(),
                            PurchaseStatus.ACTIVE,
                            startDate,
                            paymentResult.transactionId()
                    );

            purchaseStore.save(purchase);
            log.info("Membership purchase saved - purchaseId={} memberName=\"{}\" planId={} status={}",
                    purchaseId, memberName, planId, PurchaseStatus.ACTIVE);

            return purchase;

        } catch (RuntimeException exception) {

            /*
             * Payment failure or any other failure after a
             * reservation must release the reservation.
             */
            log.error("Purchase failed - memberName=\"{}\" planId={} error=\"{}\" - rolling back reservations",
                    memberName, planId, exception.getMessage());

            if (promoSlotReserved) {
                log.debug("Releasing promo slot - planId={}", planId);
                planStore.releaseSlot(planId);
            }

            if (unlimitedReserved) {
                log.debug("Releasing unlimited membership reservation - memberName=\"{}\" purchaseId={}", memberName, purchaseId);
                purchaseStore.releaseUnlimitedMembership(
                        memberName,
                        purchaseId
                );
            }

            throw exception;
        }
    }

    /**
     * Looks up a purchase by its ID.
     * Throws {@link IllegalArgumentException} if no purchase is found with the given ID.
     */
    public MembershipPurchase getPurchase(UUID purchaseId) {
        log.debug("Looking up purchase - purchaseId={}", purchaseId);
        return purchaseStore.findById(purchaseId)
                .orElseThrow(() -> {
                    log.warn("Purchase not found - purchaseId={}", purchaseId);
                    return new IllegalArgumentException(
                            "Purchase not found: " + purchaseId
                    );
                });
    }
}
