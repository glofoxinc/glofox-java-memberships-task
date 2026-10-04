package com.glofox.memberships.service;

import com.glofox.memberships.config.PlanSeeder;
import com.glofox.memberships.config.SeedData;
import com.glofox.memberships.domain.MembershipPurchase;
import com.glofox.memberships.payment.AlwaysSucceedingPaymentGateway;
import com.glofox.memberships.payment.PaymentGateway;
import com.glofox.memberships.storage.memory.PlanStore;
import com.glofox.memberships.storage.memory.PurchaseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class MembershipServiceTest {

    private PlanStore planStore;
    private PurchaseStore purchaseStore;
    private MembershipService membershipService;

    @BeforeEach
    void setUp() {
        planStore = new PlanStore();
        purchaseStore = new PurchaseStore();

        PlanSeeder planSeeder = new PlanSeeder(planStore);
        planSeeder.run();

        membershipService = new MembershipService(
                planStore,
                purchaseStore,
                new AlwaysSucceedingPaymentGateway()
        );
    }

    @Test
    void shouldRejectUnknownPlan() {
        UUID unknownPlanId =
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

        assertThrows(
                RuntimeException.class,
                () -> membershipService.purchaseMembership(
                        "Alice",
                        unknownPlanId,
                        LocalDate.of(2026, 9, 1)
                )
        );
    }

    @Test
    void shouldRejectInactivePlan() {
        assertThrows(
                RuntimeException.class,
                () -> membershipService.purchaseMembership(
                        "Alice",
                        SeedData.LEGACY_PLAN_ID,
                        LocalDate.of(2026, 9, 1)
                )
        );
    }

    @Test
    void shouldAllowOnlyOneActiveUnlimitedMembershipPerMember() {
        membershipService.purchaseMembership(
                "Alice",
                SeedData.MONTHLY_UNLIMITED_ID,
                LocalDate.of(2026, 9, 1)
        );

        assertThrows(
                RuntimeException.class,
                () -> membershipService.purchaseMembership(
                        "Alice",
                        SeedData.MONTHLY_UNLIMITED_ID,
                        LocalDate.of(2026, 10, 1)
                )
        );

        MembershipPurchase bobPurchase =
                membershipService.purchaseMembership(
                        "Bob",
                        SeedData.MONTHLY_UNLIMITED_ID,
                        LocalDate.of(2026, 9, 1)
                );

        assertNotNull(bobPurchase);
    }

    @Test
    void shouldAllowExactlyFivePromoPurchases() {
        for (int i = 0; i < 5; i++) {
            MembershipPurchase purchase =
                    membershipService.purchaseMembership(
                            "Member-" + i,
                            SeedData.LAUNCH_PROMO_PACK_ID,
                            LocalDate.of(2026, 9, 1)
                    );

            assertNotNull(purchase);
        }

        assertEquals(
                0,
                planStore.findById(SeedData.LAUNCH_PROMO_PACK_ID)
                        .getRemainingSlots()
        );

        assertThrows(
                RuntimeException.class,
                () -> membershipService.purchaseMembership(
                        "Member-6",
                        SeedData.LAUNCH_PROMO_PACK_ID,
                        LocalDate.of(2026, 9, 1)
                )
        );
    }

    @Test
    void shouldAllowExactlyFiveConcurrentPromoPurchases() throws Exception {
        int requestCount = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(requestCount);

        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();

            for (int i = 0; i < requestCount; i++) {
                int memberNumber = i;

                tasks.add(() -> {
                    try {
                        membershipService.purchaseMembership(
                                "Concurrent-Member-" + memberNumber,
                                SeedData.LAUNCH_PROMO_PACK_ID,
                                LocalDate.of(2026, 9, 1)
                        );
                        return true;
                    } catch (RuntimeException exception) {
                        return false;
                    }
                });
            }

            List<Future<Boolean>> results = executor.invokeAll(tasks);

            long successfulPurchases = results.stream()
                    .filter(future -> {
                        try {
                            return future.get();
                        } catch (Exception exception) {
                            return false;
                        }
                    })
                    .count();

            assertEquals(5, successfulPurchases);

            assertEquals(
                    0,
                    planStore.findById(SeedData.LAUNCH_PROMO_PACK_ID)
                            .getRemainingSlots()
            );

        } finally {
            executor.shutdown();
        }
    }

    @Test
    void shouldNotConsumePromoSlotWhenPaymentFails() {
        MembershipService failingPaymentService =
                new MembershipService(
                        planStore,
                        purchaseStore,
                        new FailingPaymentGateway()
                );

        assertThrows(
                PaymentGateway.PaymentFailedException.class,
                () -> failingPaymentService.purchaseMembership(
                        "Alice",
                        SeedData.LAUNCH_PROMO_PACK_ID,
                        LocalDate.of(2026, 9, 1)
                )
        );

        assertEquals(
                5,
                planStore.findById(SeedData.LAUNCH_PROMO_PACK_ID)
                        .getRemainingSlots()
        );

        assertEquals(
                0,
                purchaseStore.findAll().size()
        );
    }

    @Test
    void shouldReleaseUnlimitedMembershipReservationWhenPaymentFails() {
        MembershipService failingPaymentService =
                new MembershipService(
                        planStore,
                        purchaseStore,
                        new FailingPaymentGateway()
                );

        assertThrows(
                PaymentGateway.PaymentFailedException.class,
                () -> failingPaymentService.purchaseMembership(
                        "Alice",
                        SeedData.MONTHLY_UNLIMITED_ID,
                        LocalDate.of(2026, 9, 1)
                )
        );

        MembershipPurchase successfulRetry =
                membershipService.purchaseMembership(
                        "Alice",
                        SeedData.MONTHLY_UNLIMITED_ID,
                        LocalDate.of(2026, 9, 1)
                );

        assertNotNull(successfulRetry);
        assertEquals("Alice", successfulRetry.getMemberName());
    }

    private static class FailingPaymentGateway
            implements PaymentGateway {

        @Override
        public PaymentResult charge(
                int amountCents,
                String memberName
        ) {
            throw new PaymentFailedException("Payment failed");
        }
    }
}