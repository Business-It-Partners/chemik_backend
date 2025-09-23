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
     * Send simple HTML OTP email for institutional users (Registration)
     */
    public boolean sendOtpToEmail(String email, String otp, String username) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom(fromEmail);
            helper.setTo(email);
            // OTP visible in notification/subject line
            helper.setSubject("Chemiki Verification Code: " + otp);

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
            // OTP visible in notification/subject line
            helper.setSubject("Chemiki Reset Code: " + otp);

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
     * Create simple registration HTML email template
     */
    private String createRegistrationEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 500px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333; margin: 0; font-size: 24px;">🧪 Chemiki</h1>
                        <p style="color: #666; margin: 5px 0 0 0;">Account Verification</p>
                    </div>
                    
                    <!-- Content -->
                    <div>
                        <p style="color: #333; font-size: 16px; margin-bottom: 20px;">
                            Hi %s,
                        </p>
                        
                        <p style="color: #666; font-size: 14px; margin-bottom: 25px;">
                            Your verification code for Chemiki registration:
                        </p>
                        
                        <!-- OTP -->
                        <div style="background-color: #f8f9fa; border: 2px solid #007bff; border-radius: 6px; padding: 20px; text-align: center; margin: 25px 0;">
                            <div style="font-size: 28px; font-weight: bold; color: #007bff; letter-spacing: 4px;">
                                %s
                            </div>
                            <p style="color: #888; font-size: 12px; margin: 10px 0 0 0;">
                                Valid for 15 minutes
                            </p>
                        </div>
                        
                        <p style="color: #666; font-size: 14px; margin-bottom: 20px;">
                            Enter this code in the app to complete your registration.
                        </p>
                        
                        <p style="color: #888; font-size: 12px; margin-top: 30px;">
                            If you didn't request this, please ignore this email.
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                        <p style="color: #888; font-size: 12px; margin: 0;">
                            © 2024 Chemiki - Building stronger neighborhoods
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
     * Create simple password reset HTML email template
     */
    private String createPasswordResetEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5;">
                <div style="max-width: 500px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h1 style="color: #333; margin: 0; font-size: 24px;">🔐 Chemiki</h1>
                        <p style="color: #666; margin: 5px 0 0 0;">Password Reset</p>
                    </div>
                    
                    <!-- Content -->
                    <div>
                        <p style="color: #333; font-size: 16px; margin-bottom: 20px;">
                            Hi %s,
                        </p>
                        
                        <p style="color: #666; font-size: 14px; margin-bottom: 25px;">
                            Your password reset code for Chemiki:
                        </p>
                        
                        <!-- OTP -->
                        <div style="background-color: #f8f9fa; border: 2px solid #dc3545; border-radius: 6px; padding: 20px; text-align: center; margin: 25px 0;">
                            <div style="font-size: 28px; font-weight: bold; color: #dc3545; letter-spacing: 4px;">
                                %s
                            </div>
                            <p style="color: #888; font-size: 12px; margin: 10px 0 0 0;">
                                Valid for 15 minutes
                            </p>
                        </div>
                        
                        <p style="color: #666; font-size: 14px; margin-bottom: 20px;">
                            Enter this code in the app to reset your password.
                        </p>
                        
                        <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                            <p style="color: #856404; font-size: 13px; margin: 0;">
                                <strong>Security:</strong> Don't share this code with anyone.
                            </p>
                        </div>
                        
                        <p style="color: #888; font-size: 12px; margin-top: 30px;">
                            If you didn't request this, please ignore this email.
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                        <p style="color: #888; font-size: 12px; margin: 0;">
                            © 2024 Chemiki - Building stronger neighborhoods
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