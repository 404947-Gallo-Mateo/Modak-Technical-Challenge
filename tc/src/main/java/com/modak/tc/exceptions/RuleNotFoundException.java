package com.modak.tc.exceptions;

import com.modak.tc.models.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// status 404
@Getter
@AllArgsConstructor
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RuleNotFoundException extends RuntimeException{

    private final String userId;
    private final String notificationType;

    public RuleNotFoundException(String message, String userId, String notificationType) {
        super(message);
        this.userId = userId;
        this.notificationType = notificationType;
    }
}
