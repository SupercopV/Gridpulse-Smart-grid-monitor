package com.gridpulse.service;

import org.springframework.stereotype.Service;

@Service
public class WhatsAppNotificationService implements NotificationService {
    @Override
    public void sendCredentials(String email, String phone, String name, String username, String tempPassword) {
        System.out.println("Placeholder WhatsApp delivery to " + phone + ": Welcome " + name + ". Log in with user: " + username);
    }

    @Override
    public void sendOtp(String email, String name, String otp) {
        System.out.println("Placeholder WhatsApp OTP delivery: " + otp);
    }
}
