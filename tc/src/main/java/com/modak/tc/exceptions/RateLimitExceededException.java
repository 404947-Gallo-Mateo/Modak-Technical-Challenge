package com.modak.tc.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//status code 429
@Getter
@AllArgsConstructor
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class RateLimitExceededException extends RuntimeException {

    private final String userId;
    private final String notificationType;
    private final Integer retryAfterSeconds;

    public RateLimitExceededException(String message, String userId, String notificationType,
                                      int retryAfterSeconds) {
        super(message);
        this.userId = userId;
        this.notificationType = notificationType;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    public String getRetryAfterHeader() {
        return retryAfterSeconds != null ? String.valueOf(retryAfterSeconds) : "30";
    }
}
