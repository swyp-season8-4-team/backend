package org.swyp.dessertbee.email.service;

public interface WarningMailService {
    void sendWarningEmail(String toEmail, String reason);
}
