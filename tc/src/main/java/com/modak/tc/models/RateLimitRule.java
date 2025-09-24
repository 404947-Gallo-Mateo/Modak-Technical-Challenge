package com.modak.tc.models;

import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class RateLimitRule {
    private int maxRequests;
    private int duration;
    private TimeUnit unit;
    private double tokensPerSecond = -1.0;

    public double getTokensPerSecond() {
        if (unit == null){
            return -1.0;
        }

        tokensPerSecond = (double) maxRequests / unit.toSeconds(duration);
        return tokensPerSecond;
    }
}
