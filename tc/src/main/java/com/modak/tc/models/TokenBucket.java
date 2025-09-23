package com.modak.tc.models;

import com.modak.tc.config.RateLimitRule;
import lombok.Getter;

import java.time.Instant;
import java.util.concurrent.locks.StampedLock;

@Getter
public class TokenBucket {
    private final int capacity;
    private final double tokensPerSecond;
    private double tokens;
    private Instant lastRefillTime;
    private final StampedLock lock;

    public TokenBucket(int capacity, double tokensPerSecond) {
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.tokens = capacity;
        this.lastRefillTime = Instant.now();
        this.lock = new StampedLock();
    }

    public boolean tryConsume() {
        long stamp = lock.writeLock();
        try {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private void refill() {
        Instant now = Instant.now();
        long timeElapsedMillis = now.toEpochMilli() - lastRefillTime.toEpochMilli();

        if (timeElapsedMillis <= 0) {
            return;
        }

        double secondsElapsed = timeElapsedMillis / 1000.0;
        double tokensToAdd = secondsElapsed * tokensPerSecond;

        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }

    public double getAvailableTokens() {
        refill();
        long stamp = lock.tryOptimisticRead();
        double currentTokens = tokens;

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                currentTokens = tokens;
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return (int) Math.floor(currentTokens);
    }
    public double getTimeToNextToken() {
        long stamp = lock.readLock();
        try {
            if (tokens >= 1) {
                return 0;
            }

            double tokensNeeded = 1 - tokens;
            return tokensNeeded / tokensPerSecond;
        } finally {
            lock.unlockRead(stamp);
        }
    }

}