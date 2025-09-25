package com.modak.tc.services.Impl;

import com.modak.tc.models.DTOs.NotificationAuditDTO;
import com.modak.tc.models.NotificationResponse;
import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.models.records.NotificationRequest;
import com.modak.tc.repositories.NotificationAuditRepository;
import com.modak.tc.services.NotificationAuditService;
import com.modak.tc.services.NotificationService;
import lombok.AllArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class NotificationAuditServiceImpl implements NotificationAuditService {

    private final NotificationAuditRepository notificationAuditRepository;
    private final ModelMapper mapper;
    @Override
    public List<NotificationAuditDTO> getAllByUserId(String userId) {
        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findByUserId(userId);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllByType(NotificationType type) {
        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findByType(type);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllByTypeAndUserId(NotificationType type, String userId) {
        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findByTypeAndUserId(type, userId);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllByUserIdAndCreatedAtAfter(String userId, LocalDateTime after) {
        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findByUserIdAndCreatedAtAfter(userId, after);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllByStatus(String status) {
        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findByStatus(status);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllNotificationsByUserTypeAndDateRange(String userId, NotificationType type, LocalDateTime startDate, LocalDateTime endDate) throws BadRequestException {
        ValidateStartAndEndDate(startDate, endDate);

        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findNotificationsByUserTypeAndDateRange(userId, type, startDate, endDate);

        for(NotificationAuditEntity nE : notificationAuditEntities){
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllNotificationsByUserAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) throws BadRequestException {
        ValidateStartAndEndDate(startDate, endDate);

        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findNotificationsByUserAndDateRange(userId, startDate, endDate);

        for (NotificationAuditEntity nE : notificationAuditEntities) {
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    @Override
    public List<NotificationAuditDTO> getAllNotificationsByTypeAndDateRange(NotificationType type, LocalDateTime startDate, LocalDateTime endDate) throws BadRequestException {
        ValidateStartAndEndDate(startDate, endDate);

        List<NotificationAuditDTO> notificationAuditDTOS = new ArrayList<>();
        List<NotificationAuditEntity> notificationAuditEntities = notificationAuditRepository.findNotificationsByTypeAndDateRange(type, startDate, endDate);

        for (NotificationAuditEntity nE : notificationAuditEntities) {
            notificationAuditDTOS.add(mapper.map(nE, NotificationAuditDTO.class));
        }

        return notificationAuditDTOS;
    }

    private void ValidateStartAndEndDate(LocalDateTime startDate, LocalDateTime endDate) throws BadRequestException {
        if (endDate.isBefore(startDate)){
            throw new BadRequestException("endDate cannot be before startDate");
        }
    }
}
