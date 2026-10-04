package com.glofox.memberships.exception;

public class PromoSoldOutException extends RuntimeException {

    public PromoSoldOutException() {
        super("Launch Promo Pack is sold out");
    }
}