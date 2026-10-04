package com.glofox.memberships.exception;

public class DuplicateUnlimitedMembershipException extends RuntimeException {

    public DuplicateUnlimitedMembershipException(String memberName) {
        super("Member already has an active unlimited membership: " + memberName);
    }
}