package com.modak.tc.services;

import com.modak.tc.models.DTOs.NotificationAuditDTO;
import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.repositories.NotificationAuditRepository;
import com.modak.tc.services.Impl.NotificationAuditServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.apache.coyote.BadRequestException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class NotificationAuditServiceUnitTest {
    @Mock
    private NotificationAuditRepository notificationAuditRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private NotificationAuditServiceImpl notificationAuditService;

    private final String TEST_USER_ID = "user123";
    private final String TEST_STATUS_SUCCESS = "SUCCESS";
    private final String TEST_STATUS_FAILED = "FAILED";

    private final LocalDateTime NOW = LocalDateTime.now();
    private final LocalDateTime START_DATE = NOW.minusDays(1);
    private final LocalDateTime END_DATE = NOW;

    @Test
    void getAllByUserId_UserExists_ShouldReturnDTOsList() {
        // create
        NotificationAuditEntity entity1 = new NotificationAuditEntity(1L, NotificationType.STATUS, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditEntity entity2 = new NotificationAuditEntity(2L, NotificationType.NEWS, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2),TEST_STATUS_SUCCESS);
        List<NotificationAuditEntity> entities = Arrays.asList(entity1, entity2);

        NotificationAuditDTO dto1 = new NotificationAuditDTO(NotificationType.STATUS, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditDTO dto2 = new NotificationAuditDTO(NotificationType.NEWS, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2),TEST_STATUS_SUCCESS);

        //fake
        when(notificationAuditRepository.findByUserId(TEST_USER_ID)).thenReturn(entities);
        when(modelMapper.map(entity1, NotificationAuditDTO.class)).thenReturn(dto1);
        when(modelMapper.map(entity2, NotificationAuditDTO.class)).thenReturn(dto2);

        List<NotificationAuditDTO> result = notificationAuditService.getAllByUserId(TEST_USER_ID);

        // assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));

        verify(notificationAuditRepository).findByUserId(TEST_USER_ID);
        verify(modelMapper, times(2)).map(any(NotificationAuditEntity.class), eq(NotificationAuditDTO.class));
    }

    @Test
    void getAllByUserId_UserExistsAndDoesntHaveNotifications_ShouldReturnEmptyList() {
        // fake
        when(notificationAuditRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.emptyList());

        List<NotificationAuditDTO> result = notificationAuditService.getAllByUserId(TEST_USER_ID);

        // assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationAuditRepository).findByUserId(TEST_USER_ID);
        verify(modelMapper, never()).map(any(NotificationAuditEntity.class), eq(NotificationAuditDTO.class));
    }

    @Test
    void getAllByType_TypeExists_ShouldReturnDTOsList() {
        // create
        NotificationAuditEntity entity1 = new NotificationAuditEntity(1L, NotificationType.URGENT, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditEntity entity2 = new NotificationAuditEntity(2L, NotificationType.URGENT, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2), TEST_STATUS_FAILED);
        List<NotificationAuditEntity> entities = Arrays.asList(entity1, entity2);

        NotificationAuditDTO dto1 = new NotificationAuditDTO(NotificationType.URGENT, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditDTO dto2 = new NotificationAuditDTO(NotificationType.URGENT, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2), TEST_STATUS_FAILED);

        // fake
        when(notificationAuditRepository.findByType(NotificationType.URGENT)).thenReturn(entities);
        when(modelMapper.map(entity1, NotificationAuditDTO.class)).thenReturn(dto1);
        when(modelMapper.map(entity2, NotificationAuditDTO.class)).thenReturn(dto2);

        List<NotificationAuditDTO> result = notificationAuditService.getAllByType(NotificationType.URGENT);

        // assert
        assertEquals(2, result.size());
        verify(notificationAuditRepository).findByType(NotificationType.URGENT);
    }

    @Test
    void getAllByTypeAndUserId_AuditsExist_ShouldReturnDTOsList() {
        // create
        NotificationAuditEntity entity1 = new NotificationAuditEntity(1L, NotificationType.URGENT, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditEntity entity2 = new NotificationAuditEntity(2L, NotificationType.URGENT, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2), TEST_STATUS_FAILED);
        List<NotificationAuditEntity> entities = Arrays.asList(entity1, entity2);

        NotificationAuditDTO dto1 = new NotificationAuditDTO(NotificationType.URGENT, TEST_USER_ID, "test message 1", LocalDateTime.now().minusDays(1), TEST_STATUS_SUCCESS);
        NotificationAuditDTO dto2 = new NotificationAuditDTO(NotificationType.URGENT, TEST_USER_ID, "test message 2", LocalDateTime.now().minusDays(2), TEST_STATUS_FAILED);

        // fake
        when(notificationAuditRepository.findByTypeAndUserId(NotificationType.URGENT, TEST_USER_ID)).thenReturn(entities);
        when(modelMapper.map(entity1, NotificationAuditDTO.class)).thenReturn(dto1);
        when(modelMapper.map(entity2, NotificationAuditDTO.class)).thenReturn(dto2);

        List<NotificationAuditDTO> result = notificationAuditService.getAllByTypeAndUserId(NotificationType.URGENT, TEST_USER_ID);

        // assert
        assertEquals(2, result.size());
        verify(notificationAuditRepository).findByTypeAndUserId(NotificationType.URGENT, TEST_USER_ID);
    }

    @Test
    void getAllByUserIdAndCreatedAtAfter_AuditsExist_ShouldReturnDTOsList() {
        // create
        LocalDateTime after = NOW.minusHours(2);
        NotificationAuditEntity entity = new NotificationAuditEntity(1L, NotificationType.URGENT, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);
        List<NotificationAuditEntity> entities = Arrays.asList(entity);

        NotificationAuditDTO dto = new NotificationAuditDTO(NotificationType.URGENT, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);

        when(notificationAuditRepository.findByUserIdAndCreatedAtAfter(TEST_USER_ID, after)).thenReturn(entities);
        when(modelMapper.map(entity, NotificationAuditDTO.class)).thenReturn(dto);

        List<NotificationAuditDTO> result = notificationAuditService.getAllByUserIdAndCreatedAtAfter(TEST_USER_ID, after);

        // assert
        assertEquals(1, result.size());
        verify(notificationAuditRepository).findByUserIdAndCreatedAtAfter(TEST_USER_ID, after);
    }

    @Test
    void getAllByStatus_StatusExists_ShouldReturnDTOsList() {
        // create
        NotificationAuditEntity entity1 = new NotificationAuditEntity(1L, NotificationType.NEWS, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);
        NotificationAuditEntity entity2 = new NotificationAuditEntity(2L, NotificationType.NEWS, TEST_USER_ID, "", null, TEST_STATUS_FAILED);
        List<NotificationAuditEntity> entities = Arrays.asList(entity1, entity2);

        NotificationAuditDTO dto1 = new NotificationAuditDTO(NotificationType.NEWS, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);
        NotificationAuditDTO dto2 = new NotificationAuditDTO(NotificationType.NEWS, TEST_USER_ID, "", null, TEST_STATUS_FAILED);

        // fake
        when(notificationAuditRepository.findByStatus(any())).thenReturn(entities);
        when(modelMapper.map(entity1, NotificationAuditDTO.class)).thenReturn(dto1);
        when(modelMapper.map(entity2, NotificationAuditDTO.class)).thenReturn(dto2);

        List<NotificationAuditDTO> result = notificationAuditService.getAllByStatus(TEST_STATUS_SUCCESS);

        // assert
        assertEquals(2, result.size());
    }

    @Test
    void getAllNotificationsByUserTypeAndDateRange_ValidDates_ShouldReturnDTOsList() throws BadRequestException {
        // create
        NotificationAuditEntity entity = new NotificationAuditEntity(1L, NotificationType.WEEKLY_REPORT, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);

        List<NotificationAuditEntity> entities = Arrays.asList(entity);

        NotificationAuditDTO dto = new NotificationAuditDTO(NotificationType.WEEKLY_REPORT, TEST_USER_ID, "", null, TEST_STATUS_SUCCESS);

        // fake
        when(notificationAuditRepository.findNotificationsByUserTypeAndDateRange(TEST_USER_ID, NotificationType.WEEKLY_REPORT, START_DATE, END_DATE))
                .thenReturn(entities);
        when(modelMapper.map(entity, NotificationAuditDTO.class)).thenReturn(dto);

        List<NotificationAuditDTO> result = notificationAuditService.getAllNotificationsByUserTypeAndDateRange(
                TEST_USER_ID, NotificationType.WEEKLY_REPORT, START_DATE, END_DATE);

        // assert
        assertEquals(1, result.size());
        verify(notificationAuditRepository).findNotificationsByUserTypeAndDateRange(TEST_USER_ID, NotificationType.WEEKLY_REPORT, START_DATE, END_DATE);
    }

    @Test
    void getAllNotificationsByUserTypeAndDateRange_EndDateBeforeStartDate_ShouldThrowException__BadRequestException() {
        // create
        LocalDateTime startDate = NOW;
        LocalDateTime endDate = NOW.minusDays(1);

        // assert
        BadRequestException exception = assertThrows(BadRequestException.class, () ->
                notificationAuditService.getAllNotificationsByUserTypeAndDateRange(
                        TEST_USER_ID, NotificationType.WEEKLY_REPORT, startDate, endDate)
        );

        assertEquals("endDate cannot be before startDate", exception.getMessage());
        verify(notificationAuditRepository, never()).findNotificationsByUserTypeAndDateRange(any(), any(), any(), any());
    }

}
