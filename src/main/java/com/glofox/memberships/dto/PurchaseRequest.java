package com.glofox.memberships.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PurchaseRequest(

        @NotBlank(message = "member_name is required")
        @JsonProperty("member_name")
        String memberName,

        @NotNull(message = "plan_id is required")
        @JsonProperty("plan_id")
        UUID planId,

        @NotNull(message = "start_date is required")
        @JsonProperty("start_date")
        LocalDate startDate
) {
}