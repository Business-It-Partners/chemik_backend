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
//
//            System.out.println("✅ REGISTRATION EMAIL OTP SENT");
//            System.out.println("📧 To: " + email);
//            System.out.println("🔐 OTP: " + otp);
//            System.out.println("👤 User: " + username);
//            System.out.println("========================================");

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
//            System.out.println("\n📱 REGISTRATION SMS OTP (MOCK - DEVELOPMENT)");
//            System.out.println("========================================");
//            System.out.println("📞 To: " + phoneNumber);
//            System.out.println("🔐 OTP: " + otp);
//            System.out.println("👤 User: " + username);
//            System.out.println("📨 Message: Your Chemiki verification code is: " + otp + ". Valid for 15 minutes.");
//            System.out.println("========================================\n");

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

//            System.out.println("✅ PASSWORD RESET EMAIL OTP SENT");
//            System.out.println("📧 To: " + email);
//            System.out.println("🔐 OTP: " + otp);
//            System.out.println("👤 User: " + username);
//            System.out.println("========================================");

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
//            System.out.println("\n📱 PASSWORD RESET SMS OTP (MOCK - DEVELOPMENT)");
//            System.out.println("========================================");
//            System.out.println("📞 To: " + phoneNumber);
//            System.out.println("🔐 OTP: " + otp);
//            System.out.println("👤 User: " + username);
//            System.out.println("📨 Message: Your Chemiki password reset code is: " + otp + ". Valid for 15 minutes.");
//            System.out.println("========================================\n");

            return true;
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset SMS OTP to: " + phoneNumber);
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Create modern registration HTML email template
     */
    private String createRegistrationEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);">
                
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="min-height: 100vh;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            
                            <!-- Main Container -->
                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="max-width: 600px; margin: 0 auto;">
                                <tr>
                                    <td style="background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3);">
                                        
                                        <!-- Header with gradient -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;">
                                                    <div style="background: rgba(255,255,255,0.2); width: 80px; height: 80px; margin: 0 auto 20px; border-radius: 20px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px);">
                                                        <span style="font-size: 48px; line-height: 80px;">🧪</span>
                                                    </div>
                                                    <h1 style="margin: 0; color: white; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">Chemiki</h1>
                                                    <p style="margin: 8px 0 0; color: rgba(255,255,255,0.9); font-size: 16px; font-weight: 500;">Verify Your Account</p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Content -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="padding: 50px 40px;">
                                                    
                                                    <p style="margin: 0 0 12px; color: #1a202c; font-size: 18px; font-weight: 600;">
                                                        Hello %s! 👋
                                                    </p>
                                                    
                                                    <p style="margin: 0 0 30px; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                        Welcome to Chemiki! To complete your registration and start building stronger connections in your neighborhood, please verify your account with the code below.
                                                    </p>
                                                    
                                                    <!-- OTP Card -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 35px 0;">
                                                        <tr>
                                                            <td style="background: linear-gradient(135deg, #f6f8fb 0%%, #e9ecf1 100%%); border-radius: 12px; padding: 35px; text-align: center; border: 2px solid #e2e8f0;">
                                                                <p style="margin: 0 0 15px; color: #718096; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">
                                                                    Your Verification Code
                                                                </p>
                                                                <div style="background: white; display: inline-block; padding: 20px 40px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.07);">
                                                                    <span style="font-size: 36px; font-weight: 700; color: #667eea; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                                        %s
                                                                    </span>
                                                                </div>
                                                                <div style="margin-top: 20px; background: rgba(102, 126, 234, 0.1); padding: 12px; border-radius: 8px; display: inline-block;">
                                                                    <p style="margin: 0; color: #667eea; font-size: 13px; font-weight: 600;">
                                                                        ⏱️ Expires in 15 minutes
                                                                    </p>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <!-- Instructions -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 30px 0;">
                                                        <tr>
                                                            <td style="background: #f7fafc; border-left: 4px solid #667eea; padding: 20px; border-radius: 8px;">
                                                                <p style="margin: 0; color: #2d3748; font-size: 14px; line-height: 1.6;">
                                                                    <strong style="color: #667eea;">Next Step:</strong> Enter this code in the Chemiki app to complete your registration and unlock all features.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <!-- Security Notice -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-top: 35px;">
                                                        <tr>
                                                            <td style="padding: 20px; background: #fffbeb; border-radius: 8px; border: 1px solid #fde68a;">
                                                                <p style="margin: 0; color: #92400e; font-size: 13px; line-height: 1.5;">
                                                                    <strong>🔒 Security Tip:</strong> Never share this code with anyone. Chemiki staff will never ask for your verification code.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <p style="margin: 35px 0 0; color: #718096; font-size: 13px; line-height: 1.5;">
                                                        If you didn't create a Chemiki account, you can safely ignore this email.
                                                    </p>
                                                    
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Footer -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="background: #f8fafc; padding: 30px 40px; text-align: center; border-top: 1px solid #e2e8f0;">
                                                    <p style="margin: 0 0 8px; color: #718096; font-size: 13px;">
                                                        Building stronger neighborhoods together
                                                    </p>
                                                    <p style="margin: 0; color: #a0aec0; font-size: 12px;">
                                                        © 2024 Chemiki. All rights reserved.
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                    </td>
                                </tr>
                            </table>
                            
                        </td>
                    </tr>
                </table>
                
            </body>
            </html>
            """,
                username != null ? username : "User",
                otp
        );
    }

    /**
     * Create modern password reset HTML email template
     */
    private String createPasswordResetEmailTemplate(String username, String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%);">
                
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="min-height: 100vh;">
                    <tr>
                        <td style="padding: 40px 20px;">
                            
                            <!-- Main Container -->
                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="max-width: 600px; margin: 0 auto;">
                                <tr>
                                    <td style="background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3);">
                                        
                                        <!-- Header with gradient -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 40px 30px; text-align: center;">
                                                    <div style="background: rgba(255,255,255,0.2); width: 80px; height: 80px; margin: 0 auto 20px; border-radius: 20px; display: flex; align-items: center; justify-content: center; backdrop-filter: blur(10px);">
                                                        <span style="font-size: 48px; line-height: 80px;">🔐</span>
                                                    </div>
                                                    <h1 style="margin: 0; color: white; font-size: 32px; font-weight: 700; letter-spacing: -0.5px;">Chemiki</h1>
                                                    <p style="margin: 8px 0 0; color: rgba(255,255,255,0.9); font-size: 16px; font-weight: 500;">Password Reset Request</p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Content -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="padding: 50px 40px;">
                                                    
                                                    <p style="margin: 0 0 12px; color: #1a202c; font-size: 18px; font-weight: 600;">
                                                        Hello %s! 👋
                                                    </p>
                                                    
                                                    <p style="margin: 0 0 30px; color: #4a5568; font-size: 15px; line-height: 1.6;">
                                                        We received a request to reset your Chemiki account password. Use the verification code below to proceed with resetting your password.
                                                    </p>
                                                    
                                                    <!-- OTP Card -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 35px 0;">
                                                        <tr>
                                                            <td style="background: linear-gradient(135deg, #fff5f5 0%%, #fed7e2 100%%); border-radius: 12px; padding: 35px; text-align: center; border: 2px solid #feb2b2;">
                                                                <p style="margin: 0 0 15px; color: #c53030; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px;">
                                                                    Your Reset Code
                                                                </p>
                                                                <div style="background: white; display: inline-block; padding: 20px 40px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.07);">
                                                                    <span style="font-size: 36px; font-weight: 700; color: #e53e3e; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                                        %s
                                                                    </span>
                                                                </div>
                                                                <div style="margin-top: 20px; background: rgba(229, 62, 62, 0.1); padding: 12px; border-radius: 8px; display: inline-block;">
                                                                    <p style="margin: 0; color: #e53e3e; font-size: 13px; font-weight: 600;">
                                                                        ⏱️ Expires in 15 minutes
                                                                    </p>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <!-- Instructions -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin: 30px 0;">
                                                        <tr>
                                                            <td style="background: #f7fafc; border-left: 4px solid #e53e3e; padding: 20px; border-radius: 8px;">
                                                                <p style="margin: 0; color: #2d3748; font-size: 14px; line-height: 1.6;">
                                                                    <strong style="color: #e53e3e;">Next Step:</strong> Enter this code in the Chemiki app to create your new password and regain access to your account.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <!-- Security Warning -->
                                                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="margin-top: 35px;">
                                                        <tr>
                                                            <td style="padding: 20px; background: #fff5f5; border-radius: 8px; border: 1px solid #feb2b2;">
                                                                <p style="margin: 0 0 12px; color: #742a2a; font-size: 13px; line-height: 1.5;">
                                                                    <strong>⚠️ Security Alert:</strong> This code is for password reset only. Never share it with anyone, including Chemiki staff.
                                                                </p>
                                                                <p style="margin: 0; color: #742a2a; font-size: 13px; line-height: 1.5;">
                                                                    If you didn't request a password reset, please secure your account immediately.
                                                                </p>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                    
                                                    <p style="margin: 35px 0 0; color: #718096; font-size: 13px; line-height: 1.5;">
                                                        If you didn't request this password reset, you can safely ignore this email. Your password will remain unchanged.
                                                    </p>
                                                    
                                                </td>
                                            </tr>
                                        </table>
                                        
                                        <!-- Footer -->
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                                            <tr>
                                                <td style="background: #f8fafc; padding: 30px 40px; text-align: center; border-top: 1px solid #e2e8f0;">
                                                    <p style="margin: 0 0 8px; color: #718096; font-size: 13px;">
                                                        Building stronger neighborhoods together
                                                    </p>
                                                    <p style="margin: 0; color: #a0aec0; font-size: 12px;">
                                                        © 2024 Chemiki. All rights reserved.
                                                    </p>
                                                </td>
                                            </tr>
                                        </table>
                                        
                                    </td>
                                </tr>
                            </table>
                            
                        </td>
                    </tr>
                </table>
                
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