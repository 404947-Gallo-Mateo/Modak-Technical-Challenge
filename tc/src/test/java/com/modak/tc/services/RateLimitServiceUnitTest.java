package com.modak.tc.services;

import com.modak.tc.config.RateLimitConfig;
import com.modak.tc.models.RateLimitRule;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.services.Impl.RateLimitServiceImpl;
import com.modak.tc.exceptions.RuleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitServiceUnitTest {
    @InjectMocks
    private RateLimitServiceImpl rateLimitService;
    @Mock
    private RateLimitConfig rateLimitConfig;

    private final String USER_ID = "user123";
    private final String NON_EXISTENT_USER_ID = "nonExistent";
    private RateLimitRule rule = new RateLimitRule();

    private RateLimitRule statusRule = new RateLimitRule();
    private RateLimitRule newsRule = new RateLimitRule();

    @BeforeEach
    void setUpRules(){
        statusRule.setUnit(TimeUnit.MINUTES);
        statusRule.setDuration(10);
        statusRule.setMaxRequests(1);
        statusRule.setTokensPerSecond(statusRule.getTokensPerSecond());

        newsRule.setUnit(TimeUnit.MINUTES);
        newsRule.setDuration(30);
        newsRule.setMaxRequests(1);
        newsRule.setTokensPerSecond(statusRule.getTokensPerSecond());
    }

    // tests for method isAllowed()
    @Test
    void isAllowed_RuleExistsAndHasTokens__True() {
        // create
        rule.setMaxRequests(2);
        rule.setDuration(1);
        rule.setUnit(TimeUnit.HOURS);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("status")).thenReturn(rule);

        boolean result = rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);

        // assert
        assertTrue(result);
        verify(rateLimitConfig, times(1)).getRuleForType("status");
    }

    @Test
    void isAllowed_RuleExistsButNoTokens__False() {
        // create
        rule.setMaxRequests(1);
        rule.setDuration(10);
        rule.setUnit(TimeUnit.MINUTES);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("news")).thenReturn(rule);

        // use the only token available
        rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);
        // no tokens left, should return False
        boolean result = rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);

        // assert
        assertFalse(result);
    }

    @Test
    void isAllowed_RuleDoesntExist_ShouldThrowException__RuleNotFoundException() {
        // fake
        when(rateLimitConfig.getRuleForType("quarter-report")).thenReturn(null);

        // assert
        RuleNotFoundException exception = assertThrows(
                RuleNotFoundException.class,
                () -> rateLimitService.isAllowed(NotificationType.QUARTER_REPORT, USER_ID)
        );

        assertEquals(USER_ID, exception.getUserId());
        assertEquals("QUARTER_REPORT", exception.getNotificationType());
        assertTrue(exception.getMessage().contains("QUARTER_REPORT"));
        assertTrue(exception.getMessage().contains(USER_ID));
    }

    @Test
    void isAllowed_DifferentUsers_ShouldHaveTheirOwnBuckets() {
        // create
        rule.setMaxRequests(1);
        rule.setDuration(1);
        rule.setUnit(TimeUnit.DAYS);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        String user1 = "user1";
        String user2 = "user2";

        // fake
        when(rateLimitConfig.getRuleForType("status")).thenReturn(rule);

        // users consume 1 token of their own bucket / success (returns True)
        boolean user1FirstAttempt = rateLimitService.isAllowed(NotificationType.STATUS, user1);
        boolean user2FirstAttempt = rateLimitService.isAllowed(NotificationType.STATUS, user2);

        // users try again, should fail (not enough tokens) / failure (returns False)
        boolean user1SecondAttempt = rateLimitService.isAllowed(NotificationType.STATUS, user1);
        boolean user2SecondAttempt = rateLimitService.isAllowed(NotificationType.STATUS, user2);

        // assert
        assertTrue(user1FirstAttempt);
        assertTrue(user2FirstAttempt);
        assertFalse(user1SecondAttempt);
        assertFalse(user2SecondAttempt);
    }

    @Test
    void isAllowed_DifferentNotificationTypes_ShouldHaveTheirOwnBuckets() {
        //fake
        when(rateLimitConfig.getRuleForType("status")).thenReturn(statusRule);
        when(rateLimitConfig.getRuleForType("news")).thenReturn(newsRule);

        // each notificationType consume 1 token of their own bucket / success (returns True)
        boolean statusFirstAttempt = rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);
        boolean newsFirstAttempt = rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);

        // again, each notificationType TRY to consume 1 token (not enough tokens) // failure (returns False)
        boolean statusSecondAttempt = rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);
        boolean newsSecondAttempt = rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);

        // Assert
        assertTrue(statusFirstAttempt);
        assertTrue(newsFirstAttempt);
        assertFalse(statusSecondAttempt);
        assertFalse(newsSecondAttempt);
    }

    //tests for method getCurrentUsage()
    @Test
    void getCurrentUsage_UserHasBuckets_ShouldReturnCorrectUsage__SUCCESS() {
        //fake
        when(rateLimitConfig.getRuleForType("status")).thenReturn(statusRule);
        when(rateLimitConfig.getRuleForType("news")).thenReturn(newsRule);

        // consume tokens from STATUS bucket and NEWS Bucket from same user
        rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);
        rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);
        rateLimitService.isAllowed(NotificationType.NEWS, USER_ID);

        Map<NotificationType, Double> usage = rateLimitService.getCurrentUsage(USER_ID);

        // assert
        assertNotNull(usage);
        // all buckets of this user, all empty
        assertEquals(2, usage.size());
        assertEquals(0.0, usage.get(NotificationType.STATUS));
        assertEquals(0.0, usage.get(NotificationType.NEWS));
    }

    @Test
    void getCurrentUsage_UserHasNoBuckets_ShouldReturnEmptyMap() {
        Map<NotificationType, Double> usage = rateLimitService.getCurrentUsage(NON_EXISTENT_USER_ID);

        // assert
        assertNotNull(usage);
        assertTrue(usage.isEmpty());
    }

    @Test
    void getRemainingQuota_UserHasBucket_ShouldReturnAvailableTokens() {
        // create
        rule.setUnit(TimeUnit.MINUTES);
        rule.setMaxRequests(3);
        rule.setDuration(1);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("marketing")).thenReturn(rule);

        // use two tokens
        rateLimitService.isAllowed(NotificationType.MARKETING, USER_ID);
        rateLimitService.isAllowed(NotificationType.MARKETING, USER_ID);

        double remainingQuota = rateLimitService.getRemainingQuota(NotificationType.MARKETING, USER_ID);

        // assert
        assertEquals(1.0, remainingQuota);
    }

    @Test
    void getRemainingQuota_UserHasNoBucketAndRuleExists_ShouldReturnBucketMaxRequests() {
        // create
        rule.setUnit(TimeUnit.HOURS);
        rule.setMaxRequests(5);
        rule.setDuration(1);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("urgent")).thenReturn(rule);

        double remainingQuota = rateLimitService.getRemainingQuota(NotificationType.URGENT, NON_EXISTENT_USER_ID);

        // assert
        assertEquals(5.0, remainingQuota);
    }

    @Test
    void getRemainingQuota_RuleDoesntExists_ShouldReturnNegativeOne() {
        // fake
        when(rateLimitConfig.getRuleForType("quarter-report")).thenReturn(null);

        double remainingQuota = rateLimitService.getRemainingQuota(NotificationType.QUARTER_REPORT, USER_ID);

        // assert
        assertEquals(-1.0, remainingQuota);
    }

    @Test
    void getRetryAfterSeconds_TokensAreAvailable_ShouldReturnZero() {
        // create
        rule.setDuration(1);
        rule.setMaxRequests(3);
        rule.setUnit(TimeUnit.MINUTES);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("urgent")).thenReturn(rule);

        // call isAllowed() to create the bucket
        rateLimitService.isAllowed(NotificationType.URGENT, USER_ID);
        int retryAfter = rateLimitService.getRetryAfterSeconds(NotificationType.URGENT, USER_ID);

        // assert
        assertEquals(0, retryAfter);
    }

    @Test
    void getRetryAfterSeconds_NoTokensAvailable_ShouldReturnPositiveValue() {
        // create
        rule.setUnit(TimeUnit.DAYS);
        rule.setMaxRequests(1);
        rule.setDuration(7);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("weekly-report")).thenReturn(rule);

        // use the only token
        rateLimitService.isAllowed(NotificationType.WEEKLY_REPORT, USER_ID);
        int retryAfter = rateLimitService.getRetryAfterSeconds(NotificationType.WEEKLY_REPORT, USER_ID);

        // assert / should be around 60 seconds
        assertTrue(retryAfter > 0);
    }

    @Test
    void tokenRefill_AfterSomeTime_ShouldRefillTokens() throws InterruptedException {
        // create
        rule.setUnit(TimeUnit.SECONDS);
        rule.setMaxRequests(10);
        rule.setDuration(1);
        rule.setTokensPerSecond(rule.getTokensPerSecond());

        // fake
        when(rateLimitConfig.getRuleForType("status")).thenReturn(rule);

        // use all tokens
        for (int i = 0; i < 10; i++) {
            rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);
        }

        // no tokens left
        boolean failNoTokens = rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);

        // await refill() method
        Thread.sleep(150);

        // should exist tokens available
        boolean shouldTokensAvailable = rateLimitService.isAllowed(NotificationType.STATUS, USER_ID);

        // assert
        assertTrue(shouldTokensAvailable);
        assertFalse(failNoTokens);
    }
}
