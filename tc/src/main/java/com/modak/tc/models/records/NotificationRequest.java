package com.modak.tc.models.records;

import com.modak.tc.models.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull NotificationType type,
        @NotBlank String userId,
        @NotBlank String message
) {}
