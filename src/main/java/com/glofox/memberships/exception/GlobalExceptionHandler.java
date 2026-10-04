package com.glofox.memberships.exception;

import com.glofox.memberships.dto.ApiResponse;
import com.glofox.memberships.payment.PaymentGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlanNotFound(
            PlanNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(
                        "PLAN_NOT_FOUND",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(InactivePlanException.class)
    public ResponseEntity<ApiResponse<Void>> handleInactivePlan(
            InactivePlanException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(
                        "PLAN_INACTIVE",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(DuplicateUnlimitedMembershipException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateMembership(
            DuplicateUnlimitedMembershipException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(
                        "ACTIVE_UNLIMITED_MEMBERSHIP_EXISTS",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(PromoSoldOutException.class)
    public ResponseEntity<ApiResponse<Void>> handlePromoSoldOut(
            PromoSoldOutException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure(
                        "PROMO_SOLD_OUT",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(PaymentGateway.PaymentFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentFailure(
            PaymentGateway.PaymentFailedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.failure(
                        "PAYMENT_FAILED",
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.failure(
                        "NOT_FOUND",
                        exception.getMessage()
                ));
    }
}