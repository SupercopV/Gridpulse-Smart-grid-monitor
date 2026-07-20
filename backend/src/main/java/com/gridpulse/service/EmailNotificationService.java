package com.gridpulse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void sendCredentials(String email, String phone, String name, String username, String tempPassword) {
        System.out.println("==================================================");
        System.out.println("AUTOMATIC CREDENTIAL DELIVERY (EMAIL SIMULATOR)");
        System.out.println("To: " + email);
        System.out.println("Subject: Welcome to GridPulse");
        System.out.println("Body:");
        System.out.println("Hello " + name + ",");
        System.out.println("");
        System.out.println("Your GridPulse account has been created.");
        System.out.println("Username: " + username);
        System.out.println("Temporary Password: " + tempPassword);
        System.out.println("");
        System.out.println("Please log in and change your password immediately.");
        System.out.println("Login URL: http://localhost:5173/login");
        System.out.println("==================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("Welcome to GridPulse");
                message.setText("Hello " + name + ",\n\n" +
                        "Your GridPulse account has been created.\n\n" +
                        "Username:\n" + username + "\n\n" +
                        "Temporary Password:\n" + tempPassword + "\n\n" +
                        "Please log in and change your password immediately.\n\n" +
                        "Login URL:\nhttp://localhost:5173/login\n\n" +
                        "Regards,\nGridPulse Administration");
                mailSender.send(message);
                System.out.println("Real email successfully dispatched to SMTP host.");
            } catch (Exception e) {
                System.err.println("SMTP send failed (running local offline fallback): " + e.getMessage());
            }
        }
    }

    @Override
    public void sendOtp(String email, String name, String otp) {
        System.out.println("==================================================");
        System.out.println("OTP DELIVERY (EMAIL SIMULATOR)");
        System.out.println("To: " + email);
        System.out.println("Subject: GridPulse Password Reset OTP");
        System.out.println("Body: Hello " + name + ", your OTP code is: " + otp);
        System.out.println("==================================================");

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("GridPulse Password Reset OTP");
                message.setText("Hello " + name + ",\n\n" +
                        "Your password reset OTP code is:\n" + otp + "\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Regards,\nGridPulse Administration");
                mailSender.send(message);
                System.out.println("Real OTP email successfully dispatched to SMTP host.");
            } catch (Exception e) {
                System.err.println("SMTP OTP send failed: " + e.getMessage());
            }
        }
    }
}
