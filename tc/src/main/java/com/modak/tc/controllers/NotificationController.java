package com.modak.tc.controllers;

import com.modak.tc.models.records.NotificationRequest;
import com.modak.tc.models.NotificationResponse;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.services.NotificationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rate-limit-status/{userId}")
    public ResponseEntity<Map<NotificationType, Double>> getRateLimitStatus(
            @PathVariable String userId) {
        Map<NotificationType, Double> status = notificationService.getRateLimitStatus(userId);
        return ResponseEntity.ok(status);
    }
}
