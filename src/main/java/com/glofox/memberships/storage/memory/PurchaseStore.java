package com.glofox.memberships.storage.memory;

import com.glofox.memberships.domain.MembershipPurchase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PurchaseStore {

    private static final Logger log = LoggerFactory.getLogger(PurchaseStore.class);

    private final Map<UUID, MembershipPurchase> purchases =
            new ConcurrentHashMap<>();

    private final Map<String, UUID> unlimitedMemberships =
            new ConcurrentHashMap<>();

    /**
     * Persists a completed membership purchase into the in-memory store, keyed by its ID.
     */
    public void save(MembershipPurchase purchase) {
        log.debug("Saving purchase - purchaseId={} memberName=\"{}\" planId={} status={}",
                purchase.getId(), purchase.getMemberName(), purchase.getPlanId(), purchase.getStatus());
        purchases.put(purchase.getId(), purchase);
        log.debug("Purchase saved - purchaseId={}", purchase.getId());
    }

    /**
     * Looks up a purchase by its ID. Returns an empty Optional if no purchase exists with the given ID.
     */
    public Optional<MembershipPurchase> findById(UUID purchaseId) {
        Optional<MembershipPurchase> result = Optional.ofNullable(purchases.get(purchaseId));
        if (result.isPresent()) {
            log.debug("Purchase lookup hit - purchaseId={}", purchaseId);
        } else {
            log.debug("Purchase lookup miss - purchaseId={}", purchaseId);
        }
        return result;
    }

    /**
     * Returns all purchases currently held in the store as a new list.
     */
    public List<MembershipPurchase> findAll() {
        log.debug("Fetching all purchases - count={}", purchases.size());
        return new ArrayList<>(purchases.values());
    }

    /**
     * Atomically reserves an unlimited membership slot for a member using putIfAbsent.
     * Returns true if the reservation was successfully created, false if the member already
     * holds an active unlimited membership.
     */
    public boolean reserveUnlimitedMembership(
            String memberName,
            UUID purchaseId
    ) {
        boolean reserved = unlimitedMemberships.putIfAbsent(memberName, purchaseId) == null;
        if (reserved) {
            log.debug("Unlimited membership reserved - memberName=\"{}\" purchaseId={}", memberName, purchaseId);
        } else {
            log.debug("Unlimited membership already exists - memberName=\"{}\"", memberName);
        }
        return reserved;
    }

    /**
     * Releases an unlimited membership reservation for a member, identified by both member name
     * and purchase ID to avoid accidentally removing a reservation from a different purchase.
     * Called during purchase rollback if payment or a later step fails.
     */
    public void releaseUnlimitedMembership(
            String memberName,
            UUID purchaseId
    ) {
        boolean removed = unlimitedMemberships.remove(memberName, purchaseId);
        if (removed) {
            log.debug("Unlimited membership reservation released - memberName=\"{}\" purchaseId={}", memberName, purchaseId);
        } else {
            log.warn("Unlimited membership release had no effect - memberName=\"{}\" purchaseId={}", memberName, purchaseId);
        }
    }
}