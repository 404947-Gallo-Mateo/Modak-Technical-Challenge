package com.modak.tc.services;

import com.modak.tc.exceptions.RateLimitExceededException;
import com.modak.tc.models.NotificationResponse;
import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.models.records.NotificationRequest;
import com.modak.tc.repositories.NotificationAuditRepository;
import com.modak.tc.services.Impl.MockGateway;
import com.modak.tc.services.Impl.NotificationServiceImpl;
import com.modak.tc.services.Impl.RateLimitServiceImpl;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceUnitTest {
    @InjectMocks
    private NotificationServiceImpl notificationService;
    @Mock
    private RateLimitServiceImpl rateLimitService;
    @Mock
    private MockGateway gateway;
    @Mock
    private NotificationAuditRepository repository;

    private final String TEST_USER_ID = "user123";
    private final String TEST_MESSAGE = "Test notification message";
    private final NotificationType TEST_TYPE = NotificationType.STATUS;
    private final NotificationRequest TEST_REQUEST = new NotificationRequest(TEST_TYPE, TEST_USER_ID, TEST_MESSAGE);

    @Test
    void sendNotification_RateLimitNotExceeded_ShouldSendNotification__SUCCESS() {
        // create
        when(rateLimitService.isAllowed(TEST_TYPE, TEST_USER_ID)).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> {
            NotificationAuditEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });
        when(rateLimitService.getRemainingQuota(TEST_TYPE, TEST_USER_ID)).thenReturn(1.0);

        // fake
        NotificationResponse response = notificationService.sendNotification(TEST_REQUEST);

        // assert
        assertNotNull(response);
        assertEquals("Notification sent successfully", response.getMessage());
        assertEquals(1L, response.getNotificationId());
        assertEquals(1.0, response.getRemainingQuota());

        verify(rateLimitService, times(1)).isAllowed(TEST_TYPE, TEST_USER_ID);
        verify(gateway, times(1)).send(TEST_USER_ID, TEST_MESSAGE);
        verify(repository, times(1)).save(any(NotificationAuditEntity.class));
        verify(rateLimitService, times(1)).getRemainingQuota(TEST_TYPE, TEST_USER_ID);
    }

    @Test
    void sendNotification_RateLimitExceeded_ShouldThrowException__RateLimitExceededException() {
        // create
        int retryInSeconds = 60;

        //fake
        when(rateLimitService.isAllowed(TEST_TYPE, TEST_USER_ID)).thenReturn(false);
        when(rateLimitService.getRetryAfterSeconds(TEST_TYPE, TEST_USER_ID)).thenReturn(retryInSeconds);

        // assert
        RateLimitExceededException exception = assertThrows(
                RateLimitExceededException.class,
                () -> notificationService.sendNotification(TEST_REQUEST)
        );

        assertEquals(TEST_USER_ID, exception.getUserId());
        assertEquals(TEST_TYPE.toString(), exception.getNotificationType());
        assertEquals(retryInSeconds, exception.getRetryAfterSeconds());
        assertTrue(exception.getMessage().contains("retry after " + retryInSeconds + " seconds."));

        verify(gateway, times(0)).send(anyString(), anyString());
        verify(repository, times(0)).save(any(NotificationAuditEntity.class));
    }

    @Test
    void sendNotification_GatewayThrowsException_ShouldSaveFailedAudit() {
        // fake
        when(rateLimitService.isAllowed(TEST_TYPE, TEST_USER_ID)).thenReturn(true);

        doThrow(new RuntimeException("Gateway failure")).when(gateway).send(TEST_USER_ID, TEST_MESSAGE);

        when(repository.save(any())).thenAnswer(invocation -> {
            NotificationAuditEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.sendNotification(TEST_REQUEST)
        );

        assertEquals("Failed to send notification", exception.getMessage());
        assertNotNull(exception.getCause());
        assertEquals("Gateway failure", exception.getCause().getMessage());

        verify(repository, times(1)).save(any(NotificationAuditEntity.class));
    }

    @Test
    void sendNotification_ShouldSetCorrectAuditFields__SUCCESS() {
        // create
        LocalDateTime beforeTest = LocalDateTime.now();

        // fake
        when(rateLimitService.isAllowed(TEST_TYPE, TEST_USER_ID)).thenReturn(true);

        when(repository.save(any())).thenAnswer(invocation -> {
            NotificationAuditEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        when(rateLimitService.getRemainingQuota(TEST_TYPE, TEST_USER_ID)).thenReturn(1.0);

        notificationService.sendNotification(TEST_REQUEST);

        // assert
        verify(repository).save(argThat(entity ->
                TEST_TYPE.equals(entity.getType()) &&
                        TEST_USER_ID.equals(entity.getUserId()) &&
                        TEST_MESSAGE.equals(entity.getMessage()) &&
                        "SUCCESS".equals(entity.getStatus()) &&
                        entity.getCreatedAt().isAfter(beforeTest) &&
                        entity.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1))
        ));
    }

    @Test
    void sendNotification_ShouldSetCorrectAuditFields__FAILURE() {
        // fake
        when(rateLimitService.isAllowed(TEST_TYPE, TEST_USER_ID)).thenReturn(true);

        doThrow(new RuntimeException("Gateway error")).when(gateway).send(TEST_USER_ID, TEST_MESSAGE);

        when(repository.save(any())).thenAnswer(invocation -> {
            NotificationAuditEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return entity;
        });

        // assert
        assertThrows(RuntimeException.class, () ->
                notificationService.sendNotification(TEST_REQUEST)
        );

        verify(repository).save(argThat(entity ->
                "FAILED".equals(entity.getStatus()) &&
                        TEST_TYPE.equals(entity.getType()) &&
                        TEST_USER_ID.equals(entity.getUserId()) &&
                        TEST_MESSAGE.equals(entity.getMessage())
        ));
    }

}
