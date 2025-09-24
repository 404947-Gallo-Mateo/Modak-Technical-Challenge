package com.modak.tc.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
public class NotificationResponse {
    private final String message;
    private final String status;
    private final Long notificationId;
    private final double remainingQuota;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public static NotificationResponse success(String message, LocalDateTime timestamp,
                                               Long notificationId, double remainingQuota) {
        return new NotificationResponse(message, "SUCCESS", notificationId, remainingQuota, timestamp);
    }

    @Override
    public String toString() {
        return "NotificationResponse{" +
                "message='" + message + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                ", notificationId='" + notificationId + '\'' +
                ", remainingQuota=" + remainingQuota +
                '}';
    }
}
