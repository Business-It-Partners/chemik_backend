package com.chemiki.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from:tiwariabishek44@gmail.com}")
    private String fromEmail;

    /**
     * Generate random 4-digit OTP
     */
    public String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000); // Generates 4-digit number
        return String.valueOf(otp);
    }

    /**
     * Send professional HTML OTP email for institutional users (Registration)
     */
    public boolean sendOtpToEmail(String email, String otp, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Verify Your Chemiki Account - OTP Inside");

            String htmlContent = createRegistrationEmailTemplate(username, otp);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            System.out.println("✅ REGISTRATION EMAIL OTP SENT");
            System.out.println("📧 To: " + email);
            System.out.println("🔐 OTP: " + otp);
            System.out.println("👤 User: " + username);
            System.out.println("========================================");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send registration email OTP to: " + email);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mock SMS OTP for general users (prints to terminal)
     */
    public boolean sendOtpToPhone(String phoneNumber, String otp, String username) {
        try {
            System.out.println("\n📱 REGISTRATION SMS OTP (MOCK - DEVELOPMENT)");
            System.out.println("========================================");
            System.out.println("📞 To: " + phoneNumber);
            System.out.println("🔐 OTP: " + otp);
            System.out.println("👤 User: " + username);
            System.out.println("📨 Message: Your Chemiki verification code is: " + otp + ". Valid for 15 minutes.");
            System.out.println("========================================\n");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send registration SMS OTP to: " + phoneNumber);
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send password reset OTP via email for institutional users
     */
    public boolean sendPasswordResetOtpToEmail(String email, String otp, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Reset Your Chemiki Password - OTP Inside");

            String htmlContent = createPasswordResetEmailTemplate(username, otp);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);

            System.out.println("✅ PASSWORD RESET EMAIL OTP SENT");
            System.out.println("📧 To: " + email);
            System.out.println("🔐 OTP: " + otp);
            System.out.println("👤 User: " + username);
            System.out.println("========================================");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email OTP to: " + email);
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mock password reset SMS OTP for general users
     */
    public boolean sendPasswordResetOtpToPhone(String phoneNumber, String otp, String username) {
        try {
            System.out.println("\n📱 PASSWORD RESET SMS OTP (MOCK - DEVELOPMENT)");
            System.out.println("========================================");
            System.out.println("📞 To: " + phoneNumber);
            System.out.println("🔐 OTP: " + otp);
            System.out.println("👤 User: " + username);
            System.out.println("📨 Message: Your Chemiki password reset code is: " + otp + ". Valid for 15 minutes.");
            System.out.println("========================================\n");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset SMS OTP to: " + phoneNumber);
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create registration HTML email template
     */
    private String createRegistrationEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chemiki OTP Verification</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                            🧪 Chemiki
                        </h1>
                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;">
                            Your Neighborhood Connection Platform
                        </p>
                    </div>
                    
                    <!-- Main Content -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333333; margin-bottom: 20px; font-size: 24px;">
                            Welcome to Chemiki, %s!
                        </h2>
                        
                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                            Thank you for joining our community platform. To complete your account verification, 
                            please use the One-Time Password (OTP) below:
                        </p>
                        
                        <!-- OTP Box -->
                        <div style="background-color: #f8f9fa; border: 2px dashed #667eea; border-radius: 10px; padding: 30px; text-align: center; margin: 30px 0;">
                            <p style="color: #666666; font-size: 14px; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px;">
                                Your Verification Code
                            </p>
                            <div style="font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 10px 0;">
                                %s
                            </div>
                            <p style="color: #999999; font-size: 12px; margin-top: 15px;">
                                ⏰ This code expires in 15 minutes
                            </p>
                        </div>
                        
                        <!-- Instructions -->
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                            <p style="color: #856404; font-size: 14px; margin: 0;">
                                <strong>Instructions:</strong><br>
                                1. Return to the Chemiki app<br>
                                2. Enter this verification code<br>
                                3. Complete your registration
                            </p>
                        </div>
                        
                        <p style="color: #666666; font-size: 14px; line-height: 1.6; margin-top: 30px;">
                            If you didn't request this verification code, please ignore this email. 
                            Your account security is important to us.
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;">
                        <p style="color: #6c757d; font-size: 14px; margin-bottom: 10px;">
                            Need help? Contact our support team
                        </p>
                        <p style="color: #6c757d; font-size: 12px; margin: 0;">
                            © 2024 Chemiki. All rights reserved.<br>
                            Building stronger neighborhoods, one connection at a time.
                        </p>
                        
                        <!-- Social Links -->
                        <div style="margin-top: 20px;">
                            <a href="#" style="display: inline-block; margin: 0 10px; color: #667eea; text-decoration: none; font-size: 12px;">
                                📱 Mobile App
                            </a>
                            <a href="#" style="display: inline-block; margin: 0 10px; color: #667eea; text-decoration: none; font-size: 12px;">
                                🌐 Website
                            </a>
                            <a href="#" style="display: inline-block; margin: 0 10px; color: #667eea; text-decoration: none; font-size: 12px;">
                                📧 Support
                            </a>
                        </div>
                    </div>
                    
                </div>
            </body>
            </html>
            """,
                username != null ? username : "User",
                otp
        );
    }

    /**
     * Create password reset HTML email template
     */
    private String createPasswordResetEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Chemiki Password Reset</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #dc3545 0%%, #c82333 100%%); padding: 40px 20px; text-align: center;">
                        <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: bold;">
                            🔐 Chemiki
                        </h1>
                        <p style="color: #ffffff; margin: 10px 0 0 0; font-size: 16px; opacity: 0.9;">
                            Password Reset Request
                        </p>
                    </div>
                    
                    <!-- Main Content -->
                    <div style="padding: 40px 30px;">
                        <h2 style="color: #333333; margin-bottom: 20px; font-size: 24px;">
                            Password Reset Request
                        </h2>
                        
                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 20px;">
                            Hi %s,
                        </p>
                        
                        <p style="color: #666666; font-size: 16px; line-height: 1.6; margin-bottom: 30px;">
                            We received a request to reset your Chemiki account password. 
                            Use the One-Time Password (OTP) below to complete the reset process:
                        </p>
                        
                        <!-- OTP Box -->
                        <div style="background-color: #f8f9fa; border: 2px dashed #dc3545; border-radius: 10px; padding: 30px; text-align: center; margin: 30px 0;">
                            <p style="color: #666666; font-size: 14px; margin-bottom: 10px; text-transform: uppercase; letter-spacing: 1px;">
                                Your Reset Code
                            </p>
                            <div style="font-size: 32px; font-weight: bold; color: #dc3545; letter-spacing: 8px; margin: 10px 0;">
                                %s
                            </div>
                            <p style="color: #999999; font-size: 12px; margin-top: 15px;">
                                ⏰ This code expires in 15 minutes
                            </p>
                        </div>
                        
                        <!-- Security Notice -->
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                            <p style="color: #856404; font-size: 14px; margin: 0;">
                                <strong>Security Notice:</strong><br>
                                • Don't share this code with anyone<br>
                                • We'll never ask for your password or OTP via phone/email<br>
                                • This request was made from your registered account
                            </p>
                        </div>
                        
                        <p style="color: #666666; font-size: 14px; line-height: 1.6; margin-top: 30px;">
                            If you didn't request this password reset, please ignore this email and consider 
                            changing your password as a precaution. Your account security is important to us.
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef;">
                        <p style="color: #6c757d; font-size: 14px; margin-bottom: 10px;">
                            Need help? Contact our support team
                        </p>
                        <p style="color: #6c757d; font-size: 12px; margin: 0;">
                            © 2024 Chemiki. All rights reserved.<br>
                            Building stronger neighborhoods, one connection at a time.
                        </p>
                    </div>
                    
                </div>
            </body>
            </html>
            """,
                username != null ? username : "User",
                otp
        );
    }

    /**
     * Send OTP based on delivery method (Generic method for backward compatibility)
     */
    public boolean sendOtp(String deliveryMethod, String target, String otp, String username) {
        switch (deliveryMethod.toLowerCase()) {
            case "email":
                return sendOtpToEmail(target, otp, username);
            case "phone":
                return sendOtpToPhone(target, otp, username);
            default:
                System.err.println("❌ Unknown delivery method: " + deliveryMethod);
                return false;
        }
    }
}