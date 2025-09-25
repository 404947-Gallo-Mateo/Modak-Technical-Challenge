package com.modak.tc.repositories;

import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationAuditRepository extends JpaRepository<NotificationAuditEntity, Long> {
    List<NotificationAuditEntity> findByUserId(String userId);
    List<NotificationAuditEntity> findByType(NotificationType type);
    List<NotificationAuditEntity> findByTypeAndUserId(NotificationType type, String userId);
    List<NotificationAuditEntity> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);
    List<NotificationAuditEntity> findByStatus(String status);

    @Query("SELECT n FROM NotificationAuditEntity n WHERE " +
            "n.userId = :userId AND " +
            "n.type = :type AND " +
            "n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.createdAt DESC")
    List<NotificationAuditEntity> findNotificationsByUserTypeAndDateRange(
            @Param("userId") String userId,
            @Param("type") NotificationType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT n FROM NotificationAuditEntity n WHERE " +
            "n.userId = :userId AND " +
            "n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.createdAt DESC")
    List<NotificationAuditEntity> findNotificationsByUserAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT n FROM NotificationAuditEntity n WHERE " +
            "n.type = :type AND " +
            "n.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY n.createdAt DESC")
    List<NotificationAuditEntity> findNotificationsByTypeAndDateRange(
            @Param("type") NotificationType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
