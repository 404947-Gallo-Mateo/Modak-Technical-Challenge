package com.modak.tc.services.Impl;

import org.springframework.stereotype.Service;

@Service
public class MockGateway {
    public void send(String userId, String message) {
        System.out.println("sending message to user " + userId);
    }
}
