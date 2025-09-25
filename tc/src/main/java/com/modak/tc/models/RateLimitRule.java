package com.modak.tc.models;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

@Data
public class RateLimitRule {
    private int maxRequests;
    private int duration;
    private TimeUnit unit;
    @Getter
    private double tokensPerSecond = -1.0;

    public RateLimitRule(int maxRequests, int duration, TimeUnit unit){
        this.maxRequests = maxRequests;
        this.duration = duration;
        this.unit = unit;

        tokensPerSecond = (this.unit != null) ? (double) maxRequests / unit.toSeconds(duration) : -1.0;
    }

    public RateLimitRule(){

    }
}
