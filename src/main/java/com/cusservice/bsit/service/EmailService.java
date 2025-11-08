package com.cusservice.bsit.service;

import com.cusservice.bsit.model.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    @Value("${app.base.url:https://localhost:8443}")
    private String baseUrl;

    public void sendVerificationEmail(User user) {
        Email from = new Email(fromEmail, fromName);
        String subject = "Verify Your Email - Classroom Support System";
        Email to = new Email(user.getEmail());
        
        String textContent = "Hello " + user.getFullName() + ",\n\n" +
                "Thank you for registering! Please click the link below to verify your email:\n\n" +
                baseUrl + "/verify-email?token=" + user.getVerificationToken() + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "Classroom Support Team";
        
        String htmlContent = "<html><body>" +
                "<h2>Welcome to Classroom Support System!</h2>" +
                "<p>Hello " + user.getFullName() + ",</p>" +
                "<p>Thank you for registering! Please click the button below to verify your email:</p>" +
                "<p style='margin: 30px 0;'>" +
                "<a href='" + baseUrl + "/verify-email?token=" + user.getVerificationToken() + "' " +
                "style='background-color: #4CAF50; color: white; padding: 14px 28px; text-decoration: none; border-radius: 4px; display: inline-block;'>" +
                "Verify Email</a></p>" +
                "<p><small>This link will expire in 24 hours.</small></p>" +
                "<p>Best regards,<br>Classroom Support Team</p>" +
                "</body></html>";
        
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        sendEmail(mail);
    }

    public void sendWelcomeEmail(User user) {
        Email from = new Email(fromEmail, fromName);
        String subject = "Welcome to Classroom Support System";
        Email to = new Email(user.getEmail());
        
        String htmlContent = "<html><body>" +
                "<h2>Welcome to Classroom Support System!</h2>" +
                "<p>Hello " + user.getFullName() + ",</p>" +
                "<p>Your account has been successfully verified! 🎉</p>" +
                "<p>You can now log in and start using the platform.</p>" +
                "<p style='margin: 30px 0;'>" +
                "<a href='" + baseUrl + "/login' " +
                "style='background-color: #2196F3; color: white; padding: 14px 28px; text-decoration: none; border-radius: 4px; display: inline-block;'>" +
                "Login Now</a></p>" +
                "<p>Best regards,<br>Classroom Support Team</p>" +
                "</body></html>";
        
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        sendEmail(mail);
    }

    private void sendEmail(Mail mail) {
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email sent successfully! Status: " + response.getStatusCode());
            } else {
                System.err.println("❌ Failed to send email. Status: " + response.getStatusCode());
                System.err.println("Response body: " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to send email via SendGrid: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
